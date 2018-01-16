package praktikum5;

/* (C) 2016, Gudrun Schiedermeier, gschied@haw-landshut.de
 * Oracle Corporation Java 1.8.0_72, Linux i386 4.4.0
 * mozart (Intel Core i7-4600U CPU/2701 MHz, 4 Cores, 11776 MB RAM)
 */

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Gudrun Schiedermeier, gschied@haw-landshut.de
 * @version 2017-01-06
 */
public interface DataStore extends Serializable
{
	/**Loescht alle Eintraege der inneren Map.
	 *
	 * @param id key der MapsMap.
	 * @throws IOException falls beim Speichern ein Fehler auftritt.
	 */
	void clear(String id) throws IOException;

	/**L�scht einen Eintrag in der inneren Map.
	 *
	 * @param id key der MapsMap.
	 * @param key key der "inneren" Map.
	 * @throws IOException falls beim speichern ein Fehler auftritt.
	 */
	void deleteEntry(String id, String key) throws IOException;

	/**Loescht den Eintrag in der MapsMap zum gegebenen key, d.h. einer id.
	 *
	 * @param id key der MapsMap.
	 * @throws IOException falls beim Speichern ein Fehler auftritt.
	 */
	void deleteMap(String id) throws IOException;

	/**Liefert einen Wert aus der Map zur entsprechenden id und einem
	 * key f�r die innere Map.
	 *
	 * @param id key der MapsMap
	 * @param key key der "inneren" Map.
	 * @return Wert zum gegebenen key.
	 * @throws IllegalArgumentException wenn zur id keine Map existiert.
	 */
	String getValue(String id, String key);

	/**Erstellt zur id als Value eine neue innere Map.
	 *
	 * @param id key der MapsMap zur Unterscheidung verschiedener Maps.
	 * @throws java.io.IOException falls beim Speichern ein Fehler auftritt.
	 * @throws IllegalArgumentException wenn zur id bereits eine Map existiert.
	 */
	void newMap(String id) throws IOException;

	/**Fuegt unter der id in der inneren Map zum key einen neuen Eintrag ein.
	 *
	 * @param id key der MapsMap.
	 * @param key key der "inneren" Map.
	 * @param value Wert der "inneren" Map.
	 * @throws java.io.IOException falls beim Speichern Fehler auftreten.
	 * @throws IllegalArgumentException wenn zur id keine Map existiert.
	 */
	void putValue(String id, String key, String value) throws IOException;

	/** Serialisiert die MapsMap, speichert Sie in einer Datei.
	 *
	 * @throws IOException falls beim Speichern Fehler auftreten.
	 */
	void save() throws IOException;

}
