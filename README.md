# WAXprivacy

Idee
----
Von Xprivacy im Zusammenhang von Sperren von bestimmen Kontakten in allen Apps.<br>
Leider läuft Xprivacy nicht zusammen mit LBE.<br>
So mit muste was eingenständiges her.<br>
Da mit war WAXprivacy geboren.

Funktion
--------
Whatsapp fragt immer das Adressebuch ab.<br>
über immer alle: <br>
1. raw_contacts ab
2. dann einzelen über raw_contacts
3. vom Kontackt die einzelen eigeben Datensätze

GUI Xposed Module
----
Auswahle welche Kontaket Whatsapp sehen darf.<br>
danach manuel in whatsapp Kontakte neu einlesen

TODO
----
- Doppelt einträge entfernen datenbanke<br>
- automatisch in whatsapp Kontakte neu einlesen
- Verbesserung GUI
- Sqlite Anbindung
- Verbesserung der BroadcastReceiver im xposed module


Build & test
------------
Build Android Studio<br>
- de.robv.android.xposed:api:82 (https://github.com/rovo89/XposedBridge)

Test Geräte:<br>
Genymotion (Android 5.1.0)<br>
und<br>
Samsung Galaxy S6 Edge (Android 5.1.1)<br>

Beide mit Xposed Version 87


in Sachen Whatsapp
------------------
Auszug AGB's Ouelle:<br>
https://www.whatsapp.com/legal/?l=de#terms-of-service<br>

"Adressbuch. Du stellst uns regelmäßig die Telefonnummern von WhatsApp-Nutzern<br>
und deinen sonstigen Kontakten in deinem Mobiltelefon-Adressbuch zur Verfügung.<br>
Du bestätigst, dass du autorisiert bist, uns solche Telefonnummern zur Verfügung zu<br>
stellen, damit wir unsere Dienste anbieten können."<br>

Hatte jeder von euch die Whatsapp nuten von allen Kontaken die Autorisierung/Erlaubnis<br>
diese Handy- oder Telephonenummer an Whatsapp weiter zugeben?
Wenn ihr die frage mit NEIN beantworte habt ihr ein Problem!
- Variant 1 löschen aller Kontakte wo hier keine Erlaubnis habt
- Variant 2 ein zweites Handy mit Kontakte wo ihr die Erlaubnis habt

oder WAXprivacy nutzen<br>
Vorteil ihr habt die Kontrolle was für Nummeren an Whatsapp gehen wo ihr die Erlaubnis<br>
von denn Kontakte habt! 

Android
-------
Leider gibt es im Android OS keine Möglichkeit diese zumachen, einzelen Nummer oder Kontakte für<br>
Apps zu sperren!

License
-------

[GNU General Public License version 3](http://www.gnu.org/licenses/gpl.txt)

Copyright (c) 2017 WAXprivacy

All rights reserved

This file is part of WAXprivacy.

WAXprivacy is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your discretion) any later version.

WAXprivacy is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with WAXprivacy.  If not, see [http://www.gnu.org/licenses/](http://www.gnu.org/licenses/).
