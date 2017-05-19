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

Build & test
------------
Build Android Studio<br>
- de.robv.android.xposed:api:82 (https://github.com/rovo89/XposedBridge)

Test Geräte:<br>
Genymotion (Android 5.1.0)<br>
und<br>
Samsung Galaxy S6 Edge (Android 5.1.1)<br>

Beide mit Xposed Version 87

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

XPrivacy is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with WAXprivacy.  If not, see [http://www.gnu.org/licenses/](http://www.gnu.org/licenses/).
