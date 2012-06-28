SellCube
========

Wymagania
---------

* WorldGuard
* Vault
* LWC
* [FirstLastSeenDB](https://github.com/blue-pl/FirstLastSeenDB/downloads)
* Essentials (opcjonalnie) - używane przez teleport (choć nie wiem czy to coś lepsze od Bukkitowego teleporta)


Uprawnienia
-----------

* `sellcube.add` - pozwala utworzyć działkę
* `sellcube.sell` - pozwala sprzedać działkę
* `sellcube.rent` - pozwala wynająć działkę
* `sellcube.sell_all` - pozwala sprzedać każdą działkę (nawet te których właścicielem gracz nie jest)
* `sellcube.rent_all` - pozwala wynająć każdą działkę (nawet te których właścicielem gracz nie jest)
* `sellcube[.*].buy` - pozwala kupować / wynajmować działki
* `sellcube.tp` - pozwala na teleportację do wybranej działki
* `sellcube.find` - pozwala na teleportację do pierwszego dostępnego ogłoszenia


Komendy
-------

`<>` - parametry obowiązkowe  
`[]` - parametry dodatkowe, środkowe można pominąć wpisując `-`  
\+ **T** - wymaga kliknięcia w tabliczkę po wpisaniu polecenia  
wszystkie polecenia mają swoje odpowiedniki bez spacji tzn /sc add -> /scadd, do których można dodać własne aliasy modyfikując plik plugin.yml wewnątrz pluginu

### Admina

* `/sc add <nazwa_regionu> [lokalizacja] [właściciel] [współlokatorzy]`  + **T** - przypisuje region do nowej działki (perm `sellcube.add`)
* `/sc sell <cena> <nazwa_regionu> [lokalizacja] [właściciel]`  + **T** - przypisuje region do nowej działki i dodaje ogłoszenie sprzedaży za `cena` coinów. (perm `sellcube.sell_all`)
* `/sc rent <cena> <nazwa_regionu> [lokalizacja] [właściciel]`  + **T** - przypisuje region do nowej działki i dodaje ogłoszenie wynajmu za `cena` coinów za dzień. (perm `sellcube.rent_all`)

`lokalizacja` - nazwa grupy (patrz: config.yml), domyślnie grupa `default`  
`właściciel` - określa użytkownika który stanie się właścicielem działki / ogłoszenia, domyślnie użytkownik wpisujący polecenie  
`współlokatorzy` - określa użytkowników (oddzielonych przecinkami) którzy staną się współlokatorami działki

Tabliczka po kliknięciu chroniona jest za pomocą lwc na użytkownika wpisującego polecenie.

### Użytkownika

* `/sc sell <cena>`  + **T** - dodaje nowe ogłoszenie sprzedaży działki za `cena` coinów. (perm `sellcube.sell`)
* `/sc rent <cena>`  + **T** - dodaje nowe ogłoszenie wynajmu działki za `cena` coinów za dzień. (perm `sellcube.rent`) 
* `/sc cancel` - anuluje tworzenie działki / ogłoszenia
* `/sc find [lokalizacja]` - teleportuje do pierwszego dostępnego ogłoszenia (perm `sellcube.find`)
* `/sc tp <nazwa>` - teleportuje do działki (2 bloki przed znakiem, patrząc na znak) o nazwie `nazwa` (perm `sellcube.tp`)
* `/sc name <nazwa>`  + **T** - zmienia nazwę działki jeśli użytkownik jest jej właścicielem, w przeciwnym wypadku zmienia nazwę którą podaje się przy teleportacji (zmiana widoczna tylko dla użytkownika wydającego polecenie)
* `/sc invite <nazwa> <współlokatorzy>` - dodanie użytkowników `współlokatorzy` jako współlokatorów działki `nazwa`
* `/sc uninvite <nazwa> <współlokatorzy>` - usunięcie użytkowników `współlokatorzy` ze współlokatorów działki `nazwa`
* `/sc leave <nazwa>` - usunięcie siebie ze współlokatorów działki `nazwa` (właściciel nie jest uznawany za współlokatora i nie może się usunąć)


Komendy serwera
---------------

* `sc update` - wymusza aktualizację informacji o dostępności kupujących na wszystkich tabliczkach


Pliki konfiguracyjne
--------------------

Config.yml generowany automatycznie przy pierwszym uruchomieniu::

    max_rent_days: 14    # maksymalna ilość dni na jaką można wypożyczyć działkę
    offline_days: 21     # po ilu dniach data ostatniej wizyty użytkownika będzie wyświetlona na czerwono
    sign_updater: false  # włączenie automatycznego aktualizowania opisów wszystkich znaków co 12h
    colors:              # sekcja określająca kolor w jakim będzie wyświetlona nazwa gracza na tabliczkach
      default: f         # nazwa podstawowej grupy gracza : kolor
    location_groups:     # grupy lokalizacji
      default:           # nazwa grupy którą będzie można nadać graczom poprzez permisje
        default:         # nazwa lokalizacji
          buy:  1        # limit pozwalajacy kupić `1` działkę w lokalizacji `default`
          rent: 1        # limit pozwalajacy wynająć `1` działkę w lokalizacji `default`

Limity można określić następująco:

* `-1` - bez limitu
* `n` - limit na `n` działek (`0..n`)

Ustawienia bazy danych są w globalnym pliku konfiguracyjnym bukkita (bukkit.yml).
Przykładowa konfiguracja sekcji database::

    database:
      username: user
      isolation: SERIALIZABLE
      driver: com.mysql.jdbc.Driver
      password: pass
      url: jdbc:mysql://localhost:3306/database_name


Info na tabliczkach
-------------------


Tabele w bazie danych
---------------------

**AdSign**

    id        - identyfikator
    seller    - gracz któy utworzył ogłoszenie
    owner     - gracz któy aktualnie posiada region
    region    - nazwa regionu
    price     - cena
    active    - true - znak z ogłoszeniem, false - znak z informacją o graczu
    rental    - true - ogłoszenie wynajmu, false - ogłoszenie sprzedaży
    rented_to  - data do której wynajęty jest region
    sign_world - lokalizacja znaku
    sign_x
    sign_y
    sign_z
    location  - nazwa grupy lokalizacji
    name      - nazwa używana podczas teleportowania do tej lokalizacji

**InvitedPlayer**

    id        - identyfikator
    adsign_id - identyfikator regionu do kórego gracz został zaproszony
    player    - nazwa gracza
    name      - nazwa używana podczas teleportowania do tej lokalizacji