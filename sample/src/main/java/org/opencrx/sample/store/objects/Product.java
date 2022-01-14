/*
 * ====================================================================
 * Project:     openCRX/Store, http://www.opencrx.org/
 * Description: ProductManager
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
package org.opencrx.sample.store.objects;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Collection;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.opencrx.sample.store.common.IStandardObject;
import org.opencrx.sample.store.common.PrimaryKey;
import org.opencrx.sample.store.common.util.ApplicationContext;
import org.opencrx.sample.store.common.util.Keys;
import org.opencrx.kernel.product1.cci2.PriceLevelQuery;
import org.opencrx.kernel.product1.jmi1.PriceLevel;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;

/**
 * Product object
 * 
 * @author OAZM (initial implementation)
 * @author WFRO (port to openCRX)
 */
public final class Product implements IStandardObject {

	public Product(
		final PrimaryKey key
	) {
        Key = key;
    }

    public Product(
    ) {
    }
    
    public Product(
        org.opencrx.kernel.product1.jmi1.Product product,
        ApplicationContext context
    ) {
    	PersistenceManager pm = JDOHelper.getPersistenceManager(product);
        this.Key = new PrimaryKey(product.refGetPath().getLastSegment().toString(), false);
        this.CategoryID = null;
        if(!product.getClassification().isEmpty()) {
            org.opencrx.kernel.product1.jmi1.ProductClassification productClassification = 
                (org.opencrx.kernel.product1.jmi1.ProductClassification)product.getClassification().iterator().next();
            this.CategoryID = new PrimaryKey(productClassification.refGetPath().getLastSegment().toString(), false);
        }
        this.Title = product.getName();
        this.Details = product.getDetailedDescription();        
        this.PictureFile = product.getDescription();
        // UnitPrice
        org.opencrx.kernel.product1.jmi1.PriceLevel priceLevel = this.findPriceLevel(pm, context);
        if(priceLevel != null) {
            org.opencrx.kernel.product1.jmi1.ProductBasePrice basePrice = this.findBasePrice(
                product, 
                priceLevel, 
                context
            );
            if(basePrice != null) {
                this.UnitPrice = basePrice.getPrice().floatValue();
            }
        }
    }

    private org.opencrx.kernel.product1.jmi1.PriceLevel findPriceLevel(
    	PersistenceManager pm,
        ApplicationContext context    	
    ) {
        PriceLevelQuery priceLevelQuery = (PriceLevelQuery)pm.newQuery(org.opencrx.kernel.product1.jmi1.PriceLevel.class);
        String priceLevelName = Keys.STORE_SCHEMA + Product.PRICE_LEVEL_NAME + " [" + DecimalFormat.getCurrencyInstance(context.getConfiguredLocale()).getCurrency().getSymbol() + "]";
        System.out.println("Finding price level with name " + priceLevelName);
        priceLevelQuery.name().equalTo(priceLevelName);
        Collection<PriceLevel> priceLevels = context.getProductSegment(pm).getPriceLevel(priceLevelQuery);
        return priceLevels.isEmpty() ? 
        	null : 
        	priceLevels.iterator().next();
    }
    
    private org.opencrx.kernel.product1.jmi1.ProductBasePrice findBasePrice(
        org.opencrx.kernel.product1.jmi1.Product product,
        org.opencrx.kernel.product1.jmi1.PriceLevel priceLevel,
        ApplicationContext context
    ) {         
    	Collection<org.opencrx.kernel.product1.jmi1.ProductBasePrice> prices = product.getBasePrice();
        for(org.opencrx.kernel.product1.jmi1.ProductBasePrice price: prices) {
            if(price.getPriceCurrency() == context.getConfiguredCurrencyCode()) {
                if(price.getPriceLevel().contains(priceLevel)) {
                    return price;
                }
            }
        }
        return null;
    }
    
    public void update(
        org.opencrx.kernel.product1.jmi1.Product product,
        ApplicationContext context
    ) {
    	PersistenceManager pm = JDOHelper.getPersistenceManager(product);
        UUIDGenerator uuids = UUIDs.getGenerator();
        
        // classification
        product.getClassification().clear();
        product.getClassification().add(
            context.getProductSegment(pm).getProductClassification(this.getCategoryID().getUuid())
        );
        // name
        product.setName(
            this.getTitle()
        );
        // description
        product.setDetailedDescription(
            this.getDetails()
        );
        // picture file
        product.setDescription(
            this.getPictureFile()
        );
        // Uom
        product.setDefaultUom(
            (org.opencrx.kernel.uom1.jmi1.Uom)pm.getObjectById("xri:@openmdx:org.opencrx.kernel.uom1/provider/" + context.getProviderName() + "/segment/Root/uom/Unit")
        );
        // Update / Create price
        org.opencrx.kernel.product1.jmi1.PriceLevel priceLevel = this.findPriceLevel(pm, context);
        if(priceLevel == null) {
            String name =
                Keys.STORE_SCHEMA + Product.PRICE_LEVEL_NAME + " [" + DecimalFormat.getCurrencyInstance(context.getConfiguredLocale()).getCurrency().getSymbol() + "]";
            priceLevel = pm.newInstance(org.opencrx.kernel.product1.jmi1.PriceLevel.class);
            priceLevel.setName(name);
            priceLevel.setDescription(name);
            priceLevel.setPriceCurrency(context.getConfiguredCurrencyCode());
            priceLevel.getPriceUsage().add(new Short(Keys.PRICE_USAGE_CONSUMER));
            context.getProductSegment(pm).addPriceLevel(
                false,
                uuids.next().toString(),
                priceLevel
            );
        }
        org.opencrx.kernel.product1.jmi1.ProductBasePrice basePrice = this.findBasePrice(
            product, 
            priceLevel, 
            context
        );
        if(basePrice == null) {
            basePrice = pm.newInstance(org.opencrx.kernel.product1.jmi1.ProductBasePrice.class);
            basePrice.setPriceCurrency(context.getConfiguredCurrencyCode());
            basePrice.getUsage().add(new Short(Keys.PRICE_USAGE_CONSUMER));
            basePrice.getPriceLevel().add(priceLevel);
            basePrice.setUom(
                (org.opencrx.kernel.uom1.jmi1.Uom)pm.getObjectById("xri:@openmdx:org.opencrx.kernel.uom1/provider/" + context.getProviderName() + "/segment/Root/uom/Unit")
            );
            product.addBasePrice(
                false,
                uuids.next().toString(),
                basePrice
            );
        }
        basePrice.setPrice(new BigDecimal(this.getUnitPrice()));
    }
        
    public final PrimaryKey getKey(
    ) {
        return Key;
    }

    public final void setKey(
    	final PrimaryKey key
    ) {
        this.Key = key;
    }

    public final PrimaryKey getCategoryID(
    ) {
        return CategoryID;
    }

    public final void setCategoryID(
    	final PrimaryKey categoryID
    ) {
        CategoryID = categoryID;
    }

    public final String getTitle(
    ) {
        return Title;
    }

    public final void setTitle(
    	final String title
    ) {
        Title = title;
    }

    public final float getUnitPrice(
    ) {
        return UnitPrice;
    }

    public final void setUnitPrice(
    	final float unitPrice
    ) {
        UnitPrice = unitPrice;
    }

    public final boolean isAvailable(
    ) {
        return IsAvailable;
    }

    public final void setAvailable(
    	final boolean isAvailable
    ) {
        IsAvailable = isAvailable;
    }

    public final String getDetails(
    ) {
        return Details;
    }

    public final void setDetails(
    	final String details
    ) {
        Details = details;
    }

    public final String getPictureFile(
    ) {
        return PictureFile;
    }

    public final void setPictureFile(
    	final String pictureFile
    ) {
        PictureFile = pictureFile;
    }

    /**
     * Returns true if all properties are valid.
     */
    public final boolean isValid(
    ) {
        // Blank checks
        if (this.getTitle().equals("")) {
            return false;
        } else {
	        // All OK
	        return true;
        }
    }

    /**
     * Compares the name of the objects
     */
    public final int compareTo(
    	final Object o
    ) {
        return compareTo((Product) o);
    }

    public final int compareTo(
    	final Product o
    ) {
        if (null == this.Title) return 1;
        return (this.Title.compareTo(o.getTitle()));
    }

    /**
     * If both objects has the same name then they are equal
     */
    public final boolean equals(
    	final Object obj
    ) {
        if (obj instanceof Product) {
            final Product o = (Product) obj;
            if (o.getTitle().equals(o.getTitle())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Save the state of this object to a stream (i.e., serialize it).
     *
     * @serialData Stores all the properties one by one to the stream
     */
    private void writeObject(
    	final ObjectOutputStream s
    ) throws IOException {
        s.writeObject(this.getKey());
        s.writeUTF(this.getTitle());
        s.writeObject(this.getCategoryID());
        s.writeUTF(this.getDetails());
        s.writeUTF(this.getPictureFile());
        s.writeFloat(this.getUnitPrice());
    }

    /**
     * Reconstitute this object from a stream (i.e., deserialize it).
     */
    private void readObject(
    	final ObjectInputStream s
    ) throws IOException, ClassNotFoundException {
        this.Key = (PrimaryKey) s.readObject();
        this.Title = s.readUTF();
        this.CategoryID = (PrimaryKey) s.readObject();
        this.Details = s.readUTF();
        this.PictureFile = s.readUTF();
        this.UnitPrice = s.readFloat();
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = -611517302607057737L;
    
    public static final String PRICE_LEVEL_NAME = "Prices";
    public static final String PROP_CATEGORY_ID = "CategoryID";
    public static final String PROP_TITLE = "Title";
    public static final String PROP_UNIT_PRICE = "UnitPrice";
    public static final String PROP_IS_AVAILABLE = "IsAvailable";
    public static final String PROP_DETAILS = "Details";
    public static final String PROP_PICTURE_FILE = "PictureFile";

    private PrimaryKey Key = new PrimaryKey();
    private PrimaryKey CategoryID = new PrimaryKey();
    private String Title = "";
    private float UnitPrice = 0f;
    private boolean IsAvailable = true;
    private String Details = "";
    private String PictureFile = "";
    
}
