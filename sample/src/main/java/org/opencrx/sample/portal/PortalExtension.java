/*
 * ====================================================================
 * Project:     openCRX/Sample, http://www.opencrx.org/
 * Description: PortalExtension
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
package org.opencrx.sample.portal;

import java.util.Date;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.opencrx.kernel.backend.Base;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.control.Control;

/**
 * PortalExtension
 *
 */
public class PortalExtension extends org.opencrx.kernel.portal.PortalExtension {

	/**
	 * Sample-specific PagePrologControl
	 *
	 */
	public static class SamplePagePrologControl extends org.openmdx.portal.servlet.control.PagePrologControl {

		/**
		 * Constructor.
		 * 
		 * @param id
		 * @param locale
		 * @param localeAsIndex
		 */
		public SamplePagePrologControl(
			String id, 
			String locale,
			int localeAsIndex
		) {
			super(id, locale, localeAsIndex);
		}

		
		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.control.PagePrologControl#paint(org.openmdx.portal.servlet.ViewPort, java.lang.String, boolean)
		 */
		@Override
		public void paint(
			ViewPort p, 
			String frame, 
			boolean forEditing
		) throws ServiceException {			
			super.paint(
				p, 
				frame, 
				forEditing
			);
	        if(FRAME_PRE_PROLOG.equals(frame)) {
	        	// no-op
	        } else if(FRAME_POST_PROLOG.equals(frame)) {
	        	p.write("<!-- Custom page prolog -->");
	        }
		}

		private static final long serialVersionUID = 6704546213618055461L;

	}

	/**
	 * Sample-specific PageEpilogControl
	 *
	 */
	public static class SamplePageEpilogControl extends org.openmdx.portal.servlet.control.PageEpilogControl {

		/**
		 * Constructor.
		 * 
		 * @param id
		 * @param locale
		 * @param localeAsIndex
		 */
		public SamplePageEpilogControl(
			String id, 
			String locale,
			int localeAsIndex
		) {
			super(id, locale, localeAsIndex);
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.control.PagePrologControl#paint(org.openmdx.portal.servlet.ViewPort, java.lang.String, boolean)
		 */
		@Override
		public void paint(
			ViewPort p, 
			String frame, 
			boolean forEditing
		) throws ServiceException {			
			super.paint(
				p, 
				frame, 
				forEditing
			);
			p.write("<!-- Custom page epilog -->");
		}

		private static final long serialVersionUID = 6704546213618055461L;

	}

	/**
	 * Sample-specific ControlFactory.
	 *
	 */
	public static class SampleControlFactory extends DefaultControlFactory {

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.DefaultPortalExtension.DefaultControlFactory#createControl(java.lang.String, java.lang.String, int, java.lang.Class, java.lang.Object[])
		 */
		@Override
		public synchronized Control createControl(
	        String id,
	        String locale,
	        int localeAsIndex,
	        Class<?> controlClass,
	        Object... parameter
		) throws ServiceException {
			if(controlClass.equals(org.openmdx.portal.servlet.control.PagePrologControl.class)) {
				return new SamplePagePrologControl(
					id, 
					locale, 
					localeAsIndex 
				);
			} else if(controlClass.equals(org.openmdx.portal.servlet.control.PageEpilogControl.class)) {
				return new SamplePageEpilogControl(
					id, 
					locale, 
					localeAsIndex 
				);
			} else {
				return super.createControl(
					id, 
					locale, 
					localeAsIndex, 
					controlClass, 
					parameter
				);
			}
		}

	}

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.DefaultPortalExtension#newControlFactory()
	 */
	@Override
	protected ControlFactory newControlFactory(
	) {	
		return new SampleControlFactory();
	}

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.DefaultPortalExtension#updateObject(java.lang.Object, java.util.Map, java.util.Map, org.openmdx.portal.servlet.ApplicationContext)
	 */
	@Override
    public void updateObject(
        Object target,
        Map<String,String[]> parameterMap,
        Map<String,Attribute> fieldMap,
        ApplicationContext app
    ) {
		System.out.println(new Date() + ": PortalExtension.updateObject()");
	    super.updateObject(
	    	target, 
	    	parameterMap, 
	    	fieldMap, 
	    	app 
	    );
	    if(
	    	(target instanceof org.opencrx.sample.account1.jmi1.Gadget) &&
	    	app.getErrorMessages().isEmpty()
	    ) {
	    	org.opencrx.sample.account1.jmi1.Gadget gadget = (org.opencrx.sample.account1.jmi1.Gadget)target;
	    	String title = gadget.getTitle();
	    	if(title == null || title.length() < 10) {
	    		app.addErrorMessage(
	    			"Title must have at least 10 characters", 
	    			null
	    		);
	    	}
	    	String text = gadget.getText();
	    	if(text == null || text.length() < 20) {
	    		app.addErrorMessage(
	    			"Text must have at least 20 characters", 
	    			null
	    		);
	    	}
	    	if(app.getErrorMessages().isEmpty()) {
	    		PersistenceManager pm = JDOHelper.getPersistenceManager(target);
	    		org.opencrx.kernel.home1.jmi1.UserHome userHome = (org.opencrx.kernel.home1.jmi1.UserHome)pm.getObjectById(
	    			app.getUserHomeIdentityAsPath()
	    		);
	    		try {
					Base.getInstance().sendAlert(
						userHome,
						app.getLoginPrincipal(), 
		    			"Updated gadget is valid", 
		    			"Gadget modification", 
						(short)0, 
						30, 
						gadget 
					);
	    		} catch(Exception e) {}
	    	}
	    }
    }

	/* (non-Javadoc)
	 * @see org.opencrx.kernel.portal.PortalExtension#getTitle(org.openmdx.base.accessor.jmi.cci.RefObject_1_0, short, java.lang.String, boolean, org.openmdx.portal.servlet.ApplicationContext)
	 */
	@Override
	public String getTitle(
		RefObject_1_0 refObj, 
		short locale,
		String localeAsString, 
		boolean asShortTitle, 
		ApplicationContext app
	) {
		String newTitle = super.getTitle(refObj, locale, localeAsString, asShortTitle, app);
		return newTitle;
	}
	
	//-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 4035646662710442133L;

}
