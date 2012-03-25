SellCube 2.0
============
Plugin pozwalający na sprzedawanie i wynajmowanie regionów za pomocą odpowiednich tabliczek. Dodatkowo obsługiwane jest grupowanie terenów, limitowanie ilości posiadanych działek w każdej grupie, telport do ogłoszeń i posiadanych działek

Opis użycia
-----------
Należy postawić tabliczkę i wpisać ogłoszenie.
Potem wpisujemy w czacie komendę: `/sellcube add [lp] [r] cena region lokalizacja` i klikamy LPM na wcześniej przygotowaną tabliczkę  
Przykład: `/scadd lp 10.5 moj_region -`

LPM na tak przygotowaną aktywaną tabliczkę powoduje wyświetlenie skróconej informacji - sprzedający i cena (oraz ID i nazwa regionu jeśli aktywny jest perm: `sellcube.sell`). Jeśli było to ogłoszenie wynajmu, a tabliczka jest nieaktywna (region został wynajęty) i klikający jest tym kto wynajmuje teren, wyświetlona zostanie informacja o graczu, który utworzył ogłoszenie, cenie za wynajęcie na kolejny dzień i do kiedy teren jest wynajęty.

PPM powoduje zmianę właściciela regionu na klikającego (na okres jednego dnia w przypadku wynajmu), przelanie odpowiedniej kwoty i przedstawienie na tabliczce informacji o kupującym.

Informacje na tabliczce pokazują kto kupił region (kolory według sekcji colors w konfigu) i kiedy gracz był na serwerze (zielony - online, czarny - offline, czerwony - nieaktywny, fioletowy - na wakacjach)
Informacje aktualizowane co 12 godzin jeśli opcja `sign_updater: true` oraz przy każdym wejściu / wyjściu gracza z serwera.

To czy użytkownik jest na wakacjach plugin określa na podstawie informacji z wtyczki FirstLastSeenDB

Tabliczki chronione są za pomocą LWC w trybie publicznym dla użytkownika, który stworzył tabliczkę (aktywna i nieaktywna utworzona z parametrem 'lp') lub nowego właściciela regionu (nieaktywna utworzona bez parametru 'lp')

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
* `sellcube[.*].buy` - pozwala kupować regiony
* `sellcube[.*].rent` - pozwala wynajmować regiony
* `sellcube.lwc_pass` - pozwala używać parametru lp (/sc add lp ...)
* `sellcube.tp` - pozwala na teleportację do ostatnio kupionego ogłoszenia
* `sellcube.find` - pozwala na teleportację do pierwszego dostępnego ogłoszenia

Przykłady użycia permisji `buy` i `rent`:

* `sellcube.buy` - Pozwolenie na kupowanie regionów we wszystkich lokalizacjach
* `sellcube.grupa.rent` - Pozwolenie na wynajmowanie regionów tylko w grupie lokalizacji `grupa`

Komendy użytkownika
-------------------
* `/sc add [lp] [r] <cena> <region> <lokalizacja>` - dodaje nowe ogłoszenie sprzedaży regionu `region` za `cena` coinów w grupie `lokalizacja`. Parametr `lp` powoduje, że kupujący nie stanie się właścicielem tabliczki po zakupie. Parametr `r` powoduje, że teren jest wynajmowany, a nie sprzedawany. Podanie `-` jako `grupa` jest równoważne z `default`. (alias `/scadd`, perm `sellcube.sell`)
* `/sc cancel` - anuluje tworzenie ogłoszenia (alias `/sccancel`)
* `/sc find` - teleportuje do pierwszego dostępnego ogłoszenia (alias `/scfind`, perm `sellcube.find`)
* `/sc tp` - teleportuje do ostatnio kupionego ogłoszenia (alias `/sctp`, perm `sellcube.tp`)
* `/sc status` - tworzy jedynie informację o statusie gracza (alias `/scstatus`)

Komendy serwera
---------------
* `sc update` - wymusza aktualizację informacji o dostępności kupujących na wszystkich tabliczkach

Pliki konfiguracyjne
--------------------
Config.yml generowany automatycznie przy pierwszym uruchomieniu

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
Przykładowa konfiguracja sekcji database:

    database:
      username: user
      isolation: SERIALIZABLE
      driver: com.mysql.jdbc.Driver
      password: pass
      url: jdbc:mysql://localhost:3306/database_name