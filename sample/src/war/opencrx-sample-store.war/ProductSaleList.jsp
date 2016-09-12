<%@ page import="
java.util.Iterator,
java.util.List,
org.opencrx.sample.store.objects.Product,
org.opencrx.sample.store.common.util.*"
%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    List items = (List) request.getAttribute( "ProductList" );
%>
<table cellpadding="5" cellspacing="0" border="0" style="border: 1px solid black">
<thead>
<tr>
<th align="left" style="border-right:1px solid gray; border-bottom:1px solid gray;">Product</th>
<th width="5em" align="left" style="border-right:1px solid gray; border-bottom:1px solid gray;">Quantity</th>
<th width="5em" align="right" style="border-bottom:1px solid gray;">Total Sale</th>
</tr>
</thead>

<tbody>
<%
    float totalPrice = 0f;

Iterator iterator = items.iterator();
while( iterator.hasNext() )
{
    String columns[] = (String[]) iterator.next();

    // Get the total price of the day
    float price = Converter.getFloat( columns[2] );
    totalPrice += price;
%>
    <tr>
    <td align="left" style="border-right:1px solid gray; border-bottom:1px solid gray;"><%=columns[0]%></td>
    <td width="5em" align="right" style="border-right:1px solid gray; border-bottom:1px solid gray;"><%=columns[1]%></td>
    <td width="5em" align="right" style="border-bottom:1px solid gray;"><%=columns[2]%></td>
    </tr>
    <%
        }
    %>
</tbody>

<tfoot>
<td>&nbsp;</td>
<td>Total</td>
<td><%=Converter.getString( totalPrice )%></td>
</tfoot>
</table>