<%@ page import="
org.opencrx.sample.store.common.util.RequestHelper,
org.opencrx.sample.store.manager.CategoryManager,
org.opencrx.sample.store.common.PrimaryKey,
java.util.Iterator,
org.opencrx.sample.store.objects.Category,
java.util.Vector,
java.util.ArrayList,
java.util.ListIterator,
org.opencrx.sample.store.common.util.*,org.opencrx.sample.store.common.util.*"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!private static final String CATEGORY_ACTIONS = "CategoryActions";
    private static final String NEW_CATEGORY_NAME = "newCategory";
    private static final String CURRENT_CATEGORY_NAME = "currentCategoryName";%>
<%
    final RequestHelper requestHelper = new RequestHelper(request);
    final ResponseHelper responseHelper = new ResponseHelper( response );
    final SessionHelper sessionHelper = new SessionHelper( request );

    // 1. Get the current category
    final CategoryManager catMan = new CategoryManager(sessionHelper.getApplicationContext());
    final PrimaryKey categoryID = requestHelper.getCurrentCategoryID();
    final Category currentCategory = catMan.get( categoryID );

    // 2. Process actions performed on category list
    if( requestHelper.isActionPerformed( CATEGORY_ACTIONS ))
    {
        final String action = requestHelper.getAction();
        if( action.equals( RequestHelper.ACTION_ADD ))
        {
    final Category newCategory = new Category( );

    newCategory.setTitle( Converter.getString( request.getParameter( NEW_CATEGORY_NAME )));
    newCategory.setParentID( categoryID );

    catMan.create( newCategory );
        }
        else if( action.equals( RequestHelper.ACTION_EDIT ))
        {
    currentCategory.setTitle( Converter.getString( request.getParameter( CURRENT_CATEGORY_NAME )));
    catMan.update( currentCategory );
        }
        else if( action.equals( RequestHelper.ACTION_DELETE ))
        {
    catMan.delete( categoryID );
    out.println( responseHelper.redirect( responseHelper.shopUrl( currentCategory.getParentID() ) ) );
    return;
        }
    }
    // 2. Load the categories having the specified parent category ID
    final java.util.Map<String,Object> categories = catMan.getChildren( categoryID );

    // 3. show breadcrums
    PrimaryKey parentID = categoryID;
    final StringBuffer buffer = new StringBuffer( );

    // --------------- Show breadcrumbs --------------------
%>
<div>
<%
    do
    {
        final Category parent = catMan.get( parentID );
        if( parent == null ) break;

        // The current category is not an hyper link
        if( parentID.equals( categoryID ))
            buffer.insert(0, "<b>" + parent.getTitle() + "</b>" );
        else
            buffer.insert(0, responseHelper.categoryHyperLink(parent.getKey(),
                    parent.getTitle() ) + " &gt; "  );

        parentID = parent.getParentID();
    } while( true );

    // 4. First show the root category link. If current page is the root category,
    // then do not make it a link
    final PrimaryKey rootCategoryID = new PrimaryKey("");
    if( categoryID.equals( rootCategoryID ) )
    {
        out.println( "Categories:");
    }
    else
    {
        out.println( responseHelper.categoryHyperLink( rootCategoryID, "Categories" ) + " &gt; " );
    }
    out.println( buffer.toString() );
%>
</div>
<%
    // ------------------- Category List -----------------------

    // 5. Render categories
    if( categories.values().size() > 0 )
    {
%>
<dl style="width:480px;">
<%
        final Iterator iter = categories.values().iterator();
        while (iter.hasNext())
        {
            final Category category = (Category) iter.next();
%>
    <dd class="categoryLink">
    <%= responseHelper.categoryHyperLink(category.getKey(),
                        category.getTitle() + "(" + category.getProductCount() + ")" ) %>
    </dd>
<%
        }
%>
</dl>
<%
    }
    else
    {
%>
<br><br>
<%
    }
%>
<%
    // --------------------- Admin Console -----------------------
    if( sessionHelper.isAdmin() )
    {
%>
<div class="admin-console" >
<form method="post">
<%= requestHelper.actionType( CATEGORY_ACTIONS ) %>
<%
        if( null != currentCategory )
        {
%>

<p>
Edit Category: <input type="text" name="<%= CURRENT_CATEGORY_NAME %>"
value="<%= currentCategory.getTitle() %>">
<input type="submit" name="<%= RequestHelper.ACTION %>"
value="<%= RequestHelper.ACTION_EDIT %>" ><input type="submit" name="<%= RequestHelper.ACTION %>"
value="<%= RequestHelper.ACTION_DELETE %>" ><br>
<%
        }
%>

Add Category: <input type="text" name="<%= NEW_CATEGORY_NAME %>">
<input type="submit" name="<%= RequestHelper.ACTION %>"
value="<%= RequestHelper.ACTION_ADD %>" >
</p>

</form>
</div>
<%
    }
%>
