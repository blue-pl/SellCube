SellCube
========
Plugin pozwalający na sprzedawanie regionów za pomocą odpowiednich tabliczek.

Opis użycia
-----------
Należy postawić tabliczkę i wpisać ogłoszenie.
Potem wpisujemy w czacie komendę: `/sellcube [lp] cena region_name` i klikamy LPM na wcześniej przygotowaną tabliczkę (alias komendy - sc)
Przykład: `/sc 10.5 moj_region`

LPM na tak przygotowaną tabliczkę powoduje wyświetlenie skróconej informacji (sprzedający i cena)
PPM powoduje zmianę właściciela regionu na klikającego, przelanie odpowiedniej kwoty i przedstawienie na tabliczce informacji o kupującym.
Informacje po kupnie pokazują kto kupił region (kolory według sekcji colors w konfigu) i kiedy gracz był na serwerze (zielony - online, czarny - offline, czerwony - nieaktywny, fioletowy - na wakacjach)
Informacje aktualizowane co 12 godzin jeśli opcja _sign_updater: true_.

To czy użytkownik jest na wakacjach plugin określa na podstawie wpisów tabeli SellCube_holidays w postaci _(INT id, VARCHAR(255) user, DATE end)_

Tabliczki chronione są za pomocą LWC w trybie prywatnym dla użytkownika, który stworzył tabliczkę (aktywna i nieaktywna w trybie LP) lub nowego właściciela regionu (nieaktywna bez trybu LP)

Wymagania
---------
* WorldGuard
* PermissionsEx
* Register
* LWC
* FirstLastSeen

Permissions
-----------
* sellcube.sell - pozwala sprzedać region
* sellcube.sell_all - pozwala sprzedać każdy region (nawet te których właścicielem gracz nie jest)
* sellcube.buy - pozwala kupować regiony
* sellcube.lwc_pass - pozwala używać parametru lp (/sc lp ...)

Komendy użytkownika
-------------------
* `/sc cena nazwa` - podstawowa wersja polecenia
* `/sc lp cena nazwa` - polecenie z parametrem lp powoduje, że kupujący nie stanie się właścicielem tabliczki
* `/sc cancel` - anuluje tworzenie ogłoszenia

Komendy serwera
---------------
* `/sc update[/i]` - wymusza aktualizację informacji o dostępności kupujących na wszystkich tabliczkach

Plik konfiguracyjny
-------------------
generowany automatycznie przy pierwszym uruchomieniu

    database:
      hostname: localhost
      port: '3306'
      user: user
      password: pass
      name: database_name
    misc:
      offline_days: 21
      sign_updater: false

Dodatkowo w pliku konfiguracyjnym można dodać sekcję określająca w jakim kolorze wyświetlać nazwę użytkownika, który kupił region

    database:
      hostname: localhost # nazwa hosta
      port: '3306' # port na którym działa mysql
      user: user # użytkownik bazy danych
      password: pass # hasło użytkownika
      name: database_name # nazwa bazy danych
    colors: # kolor grupy w jakim będzie wyświetlona nazwa użytkownika na nieaktywnym ogłoszeniu (po kupieniu)
      Adm: c
      Bob: e
      Mod: 2
      Dev: b
    misc:
      offline_days: 21 # po ilu dniach data ostatniej wizyty użytkownika będzie wyświetlona na czerwono
      sign_updater: false # opcja włączenia/wyłączenia automatycznej aktualizacji znaków co 12h
