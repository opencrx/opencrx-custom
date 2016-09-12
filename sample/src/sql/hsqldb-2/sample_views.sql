create view sample_report_quote as
select
    q.object_id,
    q.contract_number,
    (select max(postal_address_line_0) from oocke1_address a inner join oocke1_address_ a_ on a.object_id = a_.object_id where a.p$$parent = q.customer and a.is_main = '1' and a.dtype = 'org:opencrx:kernel:account1:PostalAddress' and a_.objusage = 500) as customer_business_postal_address_line0,
    (select max(postal_address_line_1) from oocke1_address a inner join oocke1_address_ a_ on a.object_id = a_.object_id where a.p$$parent = q.customer and a.is_main = '1' and a.dtype = 'org:opencrx:kernel:account1:PostalAddress' and a_.objusage = 500) as customer_business_postal_address_line1,
    (select max(postal_street_0) from oocke1_address a inner join oocke1_address_ a_ on a.object_id = a_.object_id where a.p$$parent = q.customer and a.is_main = '1' and a.dtype = 'org:opencrx:kernel:account1:PostalAddress' and a_.objusage = 500) as customer_business_postal_street0,
    (select max(postal_street_1) from oocke1_address a inner join oocke1_address_ a_ on a.object_id = a_.object_id where a.p$$parent = q.customer and a.is_main = '1' and a.dtype = 'org:opencrx:kernel:account1:PostalAddress' and a_.objusage = 500) as customer_business_postal_street1,
    (select max(postal_code) from oocke1_address a inner join oocke1_address_ a_ on a.object_id = a_.object_id where a.p$$parent = q.customer and a.is_main = '1' and a.dtype = 'org:opencrx:kernel:account1:PostalAddress' and a_.objusage = 500) as customer_business_postal_code,
    (select max(postal_city) from oocke1_address a inner join oocke1_address_ a_ on a.object_id = a_.object_id where a.p$$parent = q.customer and a.is_main = '1' and a.dtype = 'org:opencrx:kernel:account1:PostalAddress' and a_.objusage = 500) as customer_business_postal_city,
    (select max(postal_country) from oocke1_address a inner join oocke1_address_ a_ on a.object_id = a_.object_id where a.p$$parent = q.customer and a.is_main = '1' and a.dtype = 'org:opencrx:kernel:account1:PostalAddress' and a_.objusage = 500) as customer_business_postal_country,
    (select max(postal_address_line_0) from oocke1_address a inner join oocke1_address_ a_ on a.object_id = a_.object_id where a.p$$parent = q.supplier and a.is_main = '1' and a.dtype = 'org:opencrx:kernel:account1:PostalAddress' and a_.objusage = 500) as supplier_business_postal_address_line0,
    (select max(postal_address_line_1) from oocke1_address a inner join oocke1_address_ a_ on a.object_id = a_.object_id where a.p$$parent = q.supplier and a.is_main = '1' and a.dtype = 'org:opencrx:kernel:account1:PostalAddress' and a_.objusage = 500) as supplier_business_postal_address_line1,
    (select max(postal_street_0) from oocke1_address a inner join oocke1_address_ a_ on a.object_id = a_.object_id where a.p$$parent = q.supplier and a.is_main = '1' and a.dtype = 'org:opencrx:kernel:account1:PostalAddress' and a_.objusage = 500) as supplier_business_postal_street0,
    (select max(postal_street_1) from oocke1_address a inner join oocke1_address_ a_ on a.object_id = a_.object_id where a.p$$parent = q.supplier and a.is_main = '1' and a.dtype = 'org:opencrx:kernel:account1:PostalAddress' and a_.objusage = 500) as supplier_business_postal_street1,
    (select max(postal_code) from oocke1_address a inner join oocke1_address_ a_ on a.object_id = a_.object_id where a.p$$parent = q.supplier and a.is_main = '1' and a.dtype = 'org:opencrx:kernel:account1:PostalAddress' and a_.objusage = 500) as supplier_business_postal_code,
    (select max(postal_city) from oocke1_address a inner join oocke1_address_ a_ on a.object_id = a_.object_id where a.p$$parent = q.supplier and a.is_main = '1' and a.dtype = 'org:opencrx:kernel:account1:PostalAddress' and a_.objusage = 500) as supplier_business_postal_city,
    (select max(postal_country) from oocke1_address a inner join oocke1_address_ a_ on a.object_id = a_.object_id where a.p$$parent = q.supplier and a.is_main = '1' and a.dtype = 'org:opencrx:kernel:account1:PostalAddress' and a_.objusage = 500) as supplier_business_postal_country,
    q.pricing_date,
    q.expires_on,
    q.total_amount,
    q.total_amount_including_tax,
    q.total_base_amount,
    q.total_discount_amount,
    q.total_sales_commission,
    q.total_tax_amount
from
    oocke1_contract q
where
    q.dtype = 'org:opencrx:kernel:contract1:Quote';

create view sample_report_quote_item as
select
    qp.object_id,
    qp.p$$parent as quote,
    qp.line_item_number,
    qp.position_number,
    qp.name as position_name,
    (select name from oocke1_product p where qp.product = p.object_id) as product_name,
    qp.quantity as quantity,
    qp.price_per_unit as price_per_unit,
    (select rate from oocke1_salestaxtype st where qp.sales_tax_type = st.object_id) as sales_tax_rate
from
    oocke1_contractposition qp
where
    qp.dtype = 'org:opencrx:kernel:contract1:QuotePosition';