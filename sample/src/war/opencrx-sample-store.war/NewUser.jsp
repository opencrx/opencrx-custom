<%@ page import="
org.opencrx.sample.store.common.util.SessionHelper,
org.opencrx.sample.store.objects.User,
org.opencrx.sample.store.common.util.*,
org.opencrx.sample.store.common.util.*,
org.opencrx.sample.store.manager.UserManager"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!private static final String ACTION_TYPE = "NewUser";%>
<%
    String userName = "";
    String confirmPassword = "";
    String password = "";
    String address = "";

    String errorMessage = null;

    SessionHelper sessionHelper = new SessionHelper( request );
    ResponseHelper responseHelper = new ResponseHelper( response );
    RequestHelper requestHelper = new RequestHelper( request );
    
    // 1. Handle post back
    if( requestHelper.isActionPerformed( ACTION_TYPE ))
    {
        // 1.1 Get the new user information from request
        userName = Converter.getString( requestHelper.getParameter( User.PROP_USER_NAME ));
        password = Converter.getString( requestHelper.getParameter( User.PROP_PASSWORD ));
        confirmPassword = Converter.getString( requestHelper.getParameter( User.PROP_PASSWORD + "1" ));
        address = Converter.getString( requestHelper.getParameter( User.PROP_ADDRESS ));

        // 1.2 Apply validation
        if( userName.length() == 0 )
        {
    errorMessage = "User name must be specified";
        }
        else if( password.length() == 0 )
        {
    errorMessage = "Password required";
        }
        else if( !password.equals( confirmPassword ) )
        {
    errorMessage = "Password does not match";
        }
        else
        {
    // 1.3 All, valid, create the new user
    User newUser = new User( );
    newUser.setName( userName );
    newUser.setPassword( confirmPassword );
    newUser.setAddress( address );
    newUser.setUserType( User.USER_TYPE_REGULAR );

    UserManager userManager = new UserManager(sessionHelper.getApplicationContext());
    userManager.create( newUser );

    // 1.4 Login the new user and reload the page
    sessionHelper.setUser( newUser );

    out.println("<p class=\"information\">Login in progress...</p>");
    
    out.println( responseHelper.redirect( request.getRequestURI() ) );
        }
    }

    if( !sessionHelper.isUserLoggedIn() )
    {
%>
<h2 class="sidebar-title">New User Registration</h2>
<div class="sidebar-block">
    <form method="POST">
    <%= requestHelper.actionType( ACTION_TYPE )%>
    <%
        if( null != errorMessage )
        {
            out.println("<p class=\"errorMessage\">" + errorMessage + "</p>");
        }
    %>
    <p>
        User Name:<br> <input name="<%= User.PROP_USER_NAME%>" value="<%= userName %>"><br>
        Password:<br> <input name="<%= User.PROP_PASSWORD%>" type="password"><br>
        Confirm Password:<br> <input name="<%= User.PROP_PASSWORD %>1" type="password"><br>
        Address:<br>
        <textarea name="<%= User.PROP_ADDRESS%>"><%= address %></textarea>
        <input type="submit" name="<%= RequestHelper.ACTION %>" value="<%= RequestHelper.ACTION_REGISTER %>" >
    </p>
    </form>
</div>
<%
    }
%>
