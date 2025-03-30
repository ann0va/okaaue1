package org.hbrs.ooka.uebung1.entities;

/**
 * Klasse zur Repräsentation eines Produkts. Dies kann um weitere Attribute gerne erweitert werden.
 */
public class Product {
    private int id = 0;
    private String name;
    private double price;

    public Product(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        // Wir lassen mal die ID eines Produkts weg, da diese nicht immer gesetzt ist bzw. ohnehin
        // über Auto-Increment in der Datenbank gesetzt wird.
        return "Product{" +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }

    /**
     * Vergleich von zwei Produkten anhand des Namens und des Preises.
     * Wichtig für den Junit-Test!!
     * @param o
     * @return
     */
    public boolean equals(Object o) {
        if (o instanceof Product) {
            Product p = (Product) o;
            return p.getName().equals(this.getName()) && p.getPrice() == this.getPrice();
        }
        return false;
    }
}
