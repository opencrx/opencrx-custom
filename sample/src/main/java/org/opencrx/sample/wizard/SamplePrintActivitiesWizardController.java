/*
 * ====================================================================
 * Project:     openCRX/Sample, http://www.opencrx.org/
 * Description: PrintActivityListWizardController
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openCRX team nor the names of the contributors
 * to openCRX may be used to endorse or promote products derived
 * from this software without specific prior written permission
 * 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 * 
 * This product includes software developed by contributors to
 * openMDX (http://www.openmdx.org/)
 */
package org.opencrx.sample.wizard;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.opencrx.kernel.account1.jmi1.Contact;
import org.opencrx.kernel.activity1.cci2.ActivityFollowUpQuery;
import org.opencrx.kernel.activity1.cci2.ActivityQuery;
import org.opencrx.kernel.activity1.cci2.EMailRecipientQuery;
import org.opencrx.kernel.activity1.jmi1.Activity;
import org.opencrx.kernel.activity1.jmi1.ActivityCategory;
import org.opencrx.kernel.activity1.jmi1.ActivityFollowUp;
import org.opencrx.kernel.activity1.jmi1.ActivityGroup;
import org.opencrx.kernel.activity1.jmi1.ActivityGroupAssignment;
import org.opencrx.kernel.activity1.jmi1.ActivityMilestone;
import org.opencrx.kernel.activity1.jmi1.ActivityProcessTransition;
import org.opencrx.kernel.activity1.jmi1.ActivityTracker;
import org.opencrx.kernel.activity1.jmi1.EMail;
import org.opencrx.kernel.activity1.jmi1.EMailRecipient;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.Codes;
import org.openmdx.portal.servlet.JspWizardController;
import org.openmdx.portal.servlet.ObjectReference;

/**
 * PrintActivityListWizardController
 *
 */
public class SamplePrintActivitiesWizardController extends JspWizardController {

	/**
	 * Worker
	 *
	 */
	private static class Worker extends Thread {

		private static int execute(
			String cmd, 
			File workingDir
		) throws Exception {
			Process process = Runtime.getRuntime().exec(cmd, null, workingDir);
			Worker worker = new Worker(process);
			worker.start();
			try {
				worker.join(60 * 1000L);
				if (worker.exit != null)
					return worker.exit;
				else
					throw new TimeoutException();
			} catch (InterruptedException ex) {
				worker.interrupt();
				throw ex;
			} finally {
				process.destroy();
			}
		}

		private final Process process;
		private Integer exit;

		private Worker(Process process) {
			this.process = process;
		}

		@Override
		public void run() {
			try {
				exit = process.waitFor();
			} catch (InterruptedException ignore) {
				return;
			}
		}
	}
	
	/**
	 * Create report.
	 * 
	 * @param activityGroup
	 * @param reportFile
	 * @param designFileOrg
	 * @param codes
	 * @param locale
	 * 
	 * @throws Exception
	 */
	protected String[] createReport(
		ActivityGroup activityGroup, 
		File reportFile, 
		File designFileOrg, 
		Codes codes, 
		short locale
	) throws ServiceException {
		PersistenceManager pm = JDOHelper.getPersistenceManager(activityGroup);
		String[] fn = reportFile.getName().split("\\.");
		File texFile = new File(reportFile.getParent(), fn[0] + ".tex");
		File designFile = new File(reportFile.getParent(), designFileOrg.getName());
		try {
			Files.copy(designFileOrg.toPath(), designFile.toPath());
		} catch(Exception e) {
			throw new ServiceException(e);
		}
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(texFile);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
		writer.println("\\input{" + designFile.getName() + "}");
		writer.println("\\begin{document}");
		define("reportVActivityGroup", maskSpecialChars(activityGroup.getName()), writer);
		ActivityQuery query = (ActivityQuery) pm.newQuery(Activity.class);
		query.orderByActivityNumber().descending();
		List<Activity> activities = activityGroup.getFilteredActivity(query);
		for (Activity activity : activities) {
			printActivity(activity, codes, locale, writer);
		}
		writer.println("\\end{document}");
		writer.close();
		try {
			return runLaTex(texFile);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Print activity.
	 * 
	 * @param activity
	 * @param codes
	 * @param locale
	 * @param writer
	 */
	protected void printActivity(
		Activity activity, 
		Codes codes, 
		short locale, 
		PrintWriter writer
	) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(activity);

		define("crxSectionVtitle", activity.getName(), writer);
		writer.println("\\begin{crxSection}");

		writer.println("\\begin{crxDescription}");
		if (activity.getDetailedDescription() != null && activity.getDetailedDescription().trim().length() > 0) {
			writer.println(maskSpecialChars(activity.getDetailedDescription()));
		}
		if (activity instanceof EMail) {
			EMail em = (EMail) activity;
			if (em.getMessageBody() != null) {
				writer.println(maskSpecialChars(em.getMessageBody()));
			}
		}
		writer.println("\\end{crxDescription}");

		writer.println("\\begin{crxActivityDetails}");
		if (activity.getProcessState() != null) {
			writer.println("\\begin{crxTabTwo}");
			macro("crxTabRowTwo", new String[] { "Status", activity.getProcessState().getName() }, writer);
			writer.println("\\end{crxTabTwo}");
		}
		if (activity.getAssignedTo() != null) {
			writer.println("\\begin{crxTabTwo}");
			macro("crxTabRowTwo", new String[] { "Assigned to", activity.getAssignedTo().getFullName() }, writer);
			writer.println("\\end{crxTabTwo}");
		}
		{
			writer.println("\\begin{crxTabTwo}");
			macro("crxTabRowTwo", new String[] { "Start at", DF.format(activity.getScheduledStart()) }, writer);
			writer.println("\\end{crxTabTwo}");
		}
		if (activity.getReportingContact() != null || activity.getReportingAccount() != null) {
			String t = "Reported by";
			writer.println("\\begin{crxTabTwo}");
			if (activity.getReportingAccount() != null) {
				macro("crxTabRowTwo", new String[] { t, activity.getReportingAccount().getFullName() }, writer);
				t = "";
			}
			if (activity.getReportingContact() != null) {
				macro("crxTabRowTwo", new String[] { t, activity.getReportingContact().getFullName() }, writer);
				t = "";
			}
			writer.println("\\end{crxTabTwo}");
		}
		Set<String> projects = new TreeSet<String>();
		Set<String> categories = new TreeSet<String>();
		Set<String> milestones = new TreeSet<String>();
		Collection<ActivityGroupAssignment> assignments = activity.getAssignedGroup();
		for (ActivityGroupAssignment assignment : assignments) {
			ActivityGroup g = assignment.getActivityGroup();
			if (g instanceof ActivityTracker) {
				projects.add(g.getName());
			} else if (g instanceof ActivityCategory) {
				categories.add(g.getName());
			} else if (g instanceof ActivityMilestone) {
				ActivityMilestone ms = (ActivityMilestone) g;
				String d = "";
				if (ms.getScheduledDate() != null) {
					d = " (" + DF.format(ms.getScheduledDate()) + ")";
				}
				milestones.add(g.getName() + d);
			}
		}
		if (!projects.isEmpty()) {
			writer.println("\\begin{crxTabTwo}");
			String l = "Project";
			for (String p : projects) {
				macro("crxTabRowTwo", new String[] { l, p }, writer);
				l = "";
			}
			writer.println("\\end{crxTabTwo}");
		}
		if (!categories.isEmpty()) {
			writer.println("\\begin{crxTabTwo}");
			String l = "Category";
			for (String p : categories) {
				macro("crxTabRowTwo", new String[] { l, p }, writer);
				l = "";
			}
			writer.println("\\end{crxTabTwo}");
		}
		if (!milestones.isEmpty()) {
			writer.println("\\begin{crxTabTwo}");
			String l = "Milestone";
			for (String p : milestones) {
				macro("crxTabRowTwo", new String[] { l, p }, writer);
				l = "";
			}
			writer.println("\\end{crxTabTwo}");
		}
		{
			writer.println("\\begin{crxTabTwo}");
			macro("crxTabRowTwo", new String[] { "Number", activity.getActivityNumber() }, writer);
			for (String el : activity.getExternalLink()) {
				if (!el.startsWith("ICAL:") && !(el.startsWith("<") && el.endsWith(">"))) {
					macro("crxTabRowTwo", new String[] { " ", el }, writer);
				}
			}
			writer.println("\\end{crxTabTwo}");
		}
		if (activity instanceof EMail) {
			EMailRecipientQuery emQuery = (EMailRecipientQuery) pm.newQuery(EMailRecipient.class);
			List<EMailRecipient> recipients = ((EMail) activity).getEmailRecipient(emQuery);
			if (!recipients.isEmpty()) {
				writer.println("\\begin{crxTabTwo}");
				for (EMailRecipient r : recipients) {
					Map<Short, String> m = codes.getShortTextByCode("org:opencrx:kernel:activity1:EMailRecipient:partyType", locale, true);
					macro("crxTabRowTwo", new String[] { m.containsKey(r.getPartyType()) ? m.get(r.getPartyType()) : "?" + Short.toString(r.getPartyType()) + "?",
							r.getParty().getAccount() != null ? r.getParty().getAccount().getFullName() : "??" }, writer);
				}
				writer.println("\\end{crxTabTwo}");
			}
		}
		writer.println("\\end{crxActivityDetails}");
		ActivityFollowUpQuery fuQuery = (ActivityFollowUpQuery) pm.newQuery(ActivityFollowUp.class);
		fuQuery.orderByCreatedAt().descending();
		List<ActivityFollowUp> followUps = activity.getFollowUp(fuQuery);
		if (!followUps.isEmpty()) {
			for (ActivityFollowUp followUp : followUps) {
				ActivityProcessTransition transition = null;
				try {
					transition = followUp.getTransition();
				} catch(Exception ignore) {}
				Contact assignedTo = null;
				try {
					assignedTo = followUp.getAssignedTo();
				} catch(Exception ignore) {}
				define("crxFollowUpVtransition", transition != null ? transition.getName() : "", writer);
				define("crxFollowUpVtitle", followUp.getTitle() != null ? followUp.getTitle() : "", writer);
				define("crxFollowUpVdate", TF.format(followUp.getCreatedAt()), writer);
				define("crxFollowUpVassignedTo", assignedTo != null ? assignedTo.getFullName() : "", writer);
				writer.println("\\begin{crxFollowUp}");
				if (followUp.getText() != null) {
					writer.println(maskSpecialChars(followUp.getText()));
				}
				writer.println("\\end{crxFollowUp}");
			}
		}
		writer.println("\\end{crxSection}");
	}

	/**
	 * Run LaTex.
	 * 
	 * @param texFile
	 * @throws Exception
	 */
	protected String[] runLaTex(
		File texFile
	) throws Exception {
		File dir = texFile.getParentFile();
		String cmd = "pdflatex -draftmode -halt-on-error -interaction batchmode " + texFile.getName();
		int status = Worker.execute(cmd, dir);
		if(status == 0) {
			cmd = "pdflatex -halt-on-error -interaction batchmode " + texFile.getName();
			Worker.execute(cmd, dir);
			String fileName = texFile.getName().split("\\.")[0];
			File resultFile = new File(dir, fileName + ".pdf");
			File downloadFile = new File(dir, fileName);
			resultFile.renameTo(downloadFile);
			return new String[]{"activityList.pdf", "text/pdf"};
		} else {
			String fileName = texFile.getName().split("\\.")[0];
			File resultFile = new File(dir, fileName + ".log");
			if(!resultFile.exists()) {
				PrintWriter pw = new PrintWriter(resultFile, "UTF-8");
				pw.println("ERROR: unable to execute " + cmd);
				pw.close();
			}
			File downloadFile = new File(dir, fileName);
			resultFile.renameTo(downloadFile);
			return new String[]{"error.log", "text/plain"};
		}
	}

	/**
	 * Print def tag.
	 * 
	 * @param name
	 * @param value
	 * @param writer
	 */
	protected void define(
		String name, 
		String value, 
		PrintWriter writer
	) {
		writer.print("\\def");
		writer.print("\\" + name);
		writer.println("{" + maskSpecialChars(value) + "}%");
	}

	/**
	 * Print macro tag.
	 * 
	 * @param name
	 * @param args
	 * @param writer
	 */
	protected void macro(
		String name, 
		String[] args, 
		PrintWriter writer
	) {
		writer.print("\\" + name);
		for (String arg : args) {
			writer.print("{" + maskSpecialChars(arg) + "}");
		}
		writer.println("");
	}

	/**
	 * Escape special characters.
	 * 
	 * @param word
	 * @return
	 */
	protected String maskSpecialChars(
		String word
	) {
		StringBuilder sb = new StringBuilder();

		char[] a = word.toCharArray();

		for (int i = 0; i < a.length; i++) {
			if (a[i] == '\\') {
				sb.append("\\textbackslash{}");
			} else if (a[i] == '_') {
				sb.append("\\_");
			} else if (a[i] == '^') {
				sb.append("\\textasciicircum{}");
			} else if (a[i] == '<') {
				sb.append("\\textless{}");
			} else if (a[i] == '>') {
				sb.append("\\textgreater{}");
			} else if (a[i] == '$') {
				sb.append("\\$");
			} else if (a[i] == '&') {
				sb.append("\\&");
			} else if (a[i] == '#') {
				sb.append("\\#");
			} else if (a[i] == '{') {
				sb.append("\\{");
			} else if (a[i] == '}') {
				sb.append("\\}");
			} else if (a[i] == '%') {
				sb.append("\\%");
			} else if (a[i] == '~') {
				sb.append("\\textasciitilde{}");
			} else if (a[i] == '"') {
				sb.append("\"'");
			} else if ((int) a[i] < 32) {
				sb.append("?");
		    } else if ((int) a[i] >= 127 && (int) a[i] <= 159) {
		    	sb.append("?");
	        } else if ((int) a[i] > 255) {
	        	sb.append("?");
	        } else {
	        	sb.append(a[i]);
	        }
		}
		return sb.toString();
	}

	/**
	 * Cancel action.
	 * 
	 * @throws ServiceException
	 */
	public void doCancel(
	) throws ServiceException {
		this.setExitAction(
			new ObjectReference(this.getObject(), this.getApp()).getSelectObjectAction()
		);
	}

	/**
	 * OK action.
	 * 
	 * @throws ServiceException
	 */
	public void doOK(
	) throws ServiceException {
		ApplicationContext app = this.getApp();
		RefObject_1_0 obj = this.getObject();
		if(obj instanceof ActivityGroup) {
			ActivityGroup activityGroup = (ActivityGroup)obj;
			String location = org.opencrx.kernel.utils.Utils.getUidAsString();
			File reportFile = new File(app.getTempFileName(location, ""));
			File designFile = new File(this.getRequest().getServletContext().getRealPath("/wizards/SamplePrintActivitiesWizard/activityList_en_US.tex"));
			String[] downloadFile = this.createReport(
				activityGroup,
				reportFile, 
				designFile,
				app.getCodes(), 
				app.getCurrentLocaleAsIndex()
			);
			this.downloadFileAction =
				new Action(
					Action.EVENT_DOWNLOAD_FROM_LOCATION,
					new Action.Parameter[]{
						new Action.Parameter(Action.PARAMETER_LOCATION, location),
						new Action.Parameter(Action.PARAMETER_NAME, downloadFile[0]),
						new Action.Parameter(Action.PARAMETER_MIME_TYPE, downloadFile[1])
					},
					app.getTexts().getClickToDownloadText() + " " + downloadFile[0],
					true
				);
		}
	}

	/**
	 * @return the downloadFileAction
	 */
	public Action getDownloadFileAction() {
		return downloadFileAction;
	}

	//-----------------------------------------------------------------------
	// Members
	//-----------------------------------------------------------------------
	private static final DateFormat DF = new SimpleDateFormat("dd.MM.yyyy");
	private static final DateFormat TF = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	
	private Action downloadFileAction;

}
