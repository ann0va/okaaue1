package org.hbrs.ooka.uebung1;

import org.hbrs.ooka.uebung1.entities.Product;
import java.util.List;

/**
 * Spezifikation des Interfaces ProductManagementInt:
 *
 * 1.
 * Zunächst MUSS ein externer Client (außerhalb der Komponente!) mit der Methode
 * openConnection() eine Session explizit öffnen!" );
 *
 * 2.
 * Methoden zur Suche, Einfügen usw. können beliebig ausgeführt werden.
 *
 * 3.
 * Dann MUSS ein externer Client mit der Methode closeConnection() die Session explizit schließen!
 */

public interface ProductManagementInt {
    // Auswahl von CRUD-Methoden (weitere können hinzugefügt werden)
    public List<Product> getProductByName(String name);

    // Lifecycle-Methoden (dürfen nicht verändert werden, siehe Spezifikation im Kommentar

    // Öffnen einer Session (hier sollte die Verbindung zur Datenbank hergestellt werden)
    public void openSession();

    // Schließen einer Session (hier sollte die Verbindung zur Datenbank geschlossen werden)
    public void closeSession();
}
