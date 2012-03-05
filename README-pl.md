SellCube 1.0
============
Plugin pozwalający na sprzedawanie regionów za pomocą odpowiednich tabliczek.

Opis użycia
-----------
Należy postawić tabliczkę i wpisać ogłoszenie.
Potem wpisujemy w czacie komendę: `/sellcube add [lp] cena region_name` i klikamy LPM na wcześniej przygotowaną tabliczkę
Przykład: `/scadd 10.5 moj_region`

LPM na tak przygotowaną tabliczkę powoduje wyświetlenie skróconej informacji - sprzedający i cena (oraz ID i nazwa regionu jeśli aktywny jest perm: _sellcube.sell_)
PPM powoduje zmianę właściciela regionu na klikającego, przelanie odpowiedniej kwoty i przedstawienie na tabliczce informacji o kupującym.
Informacje po kupnie pokazują kto kupił region (kolory według sekcji colors w konfigu) i kiedy gracz był na serwerze (zielony - online, czarny - offline, czerwony - nieaktywny, fioletowy - na wakacjach)
Informacje aktualizowane co 12 godzin jeśli opcja _sign_updater: true_.

To czy użytkownik jest na wakacjach plugin określa na podstawie informacji z wtyczki FirstLastSeenDB

Tabliczki chronione są za pomocą LWC w trybie publicznym dla użytkownika, który stworzył tabliczkę (aktywna i nieaktywna w trybie LP) lub nowego właściciela regionu (nieaktywna bez trybu LP)

Wymagania
---------
* WorldGuard
* PermissionsEx
* Register
* LWC
* [FirstLastSeenDB](https://github.com/blue-pl/FirstLastSeenDB/downloads)
* Essentials (opcjonalnie) - używane przez teleport

Permissions
-----------
* `sellcube.sell` - pozwala sprzedać region
* `sellcube.sell_all` - pozwala sprzedać każdy region (nawet te których właścicielem gracz nie jest)
* `sellcube.buy` - pozwala kupować regiony
* `sellcube.lwc_pass` - pozwala używać parametru lp (/sc add lp ...)
* `sellcube.tp` - pozwala na teleportację do ostatnio kupionego ogłoszenia
* `sellcube.find` - pozwala na teleportację do pierwszego dostępnego ogłoszenia

Komendy użytkownika
-------------------
* `/sc add cena nazwa` - dodaje nowe ogłoszenie sprzedaży regionu `nazwa` za `cena` coinów (alias `/scadd`)
* `/sc add lp cena nazwa` - polecenie z parametrem lp powoduje, że kupujący nie stanie się właścicielem tabliczki po zakupie (alias `/scadd`)
* `/sc copy db_id` - tworzy kopię ogłoszenia na podstawie id wpisu z bazy (LWC na właściciela ogłoszenia) (alias `/sccopy`)
* `/sc cancel` - anuluje tworzenie ogłoszenia (alias `/sccancel`)
* `/sc find` - teleportuje do pierwszego dostępnego ogłoszenia (alias `/scfind`)
* `/sc status` - tworzy jedynie informację o statusie gracza (alias `/scstatus`)
* `/sc tp` - teleportuje do ostatnio kupionego ogłoszenia (alias `/sctp`)

Komendy serwera
---------------
* `sc update` - wymusza aktualizację informacji o dostępności kupujących na wszystkich tabliczkach

Pliki konfiguracyjne
--------------------
Config.yml generowany automatycznie przy pierwszym uruchomieniu

    offline_days: 21
    sign_updater: false

Dodatkowo w pliku konfiguracyjnym można dodać sekcję określająca w jakim kolorze wyświetlać nazwę użytkownika, który kupił region

    offline_days: 21 # po ilu dniach data ostatniej wizyty użytkownika będzie wyświetlona na czerwono
    sign_updater: false # opcja włączenia/wyłączenia automatycznej aktualizacji znaków co 12h
    colors: # kolor grupy w jakim będzie wyświetlona nazwa użytkownika na nieaktywnym ogłoszeniu (po kupieniu)
      Adm: c
      Bob: e
      Mod: 2
      Dev: b

Ustawienia bazy danych są w globalnym pliku konfiguracyjnym bukkita (bukkit.yml).
Przykładowa konfiguracja sekcji database:

    database:
      username: user
      isolation: SERIALIZABLE
      driver: com.mysql.jdbc.Driver
      password: pass
      url: jdbc:mysql://localhost:3306/database_name