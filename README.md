<div align="center">
  <img src="app/src/main/res/drawable-nodpi/icon_dark_fit.png" width="210" alt="DailyQuest logo" />

  <h1>DailyQuest</h1>

  <p>
    <b>Aplikacja do nauki w stylu techniki Pomodoro i prowadzenia listy zadań to-do.</b>
  </p>

  <p>
    DailyQuest pomaga zapisać zadania, pracować w 25-minutowych sesjach skupienia
    i śledzić swoje postępy w prosty, motywujący sposób.
  </p>
</div>

---

## Wygląd aplikacji

DailyQuest ma spokojny, 'graficzny' styl. Interfejs korzysta z kremowego tła, ciemnego tekstu i granatowego koloru jako głównego akcentu. Poniżej są przedstawione główne obrazki i kolory użyte do stworzenia tego projektu.

<div>
  <span style="display:inline-block;width:130px;padding:12px;margin:4px;background:#F6F0E4;border:1px solid #EFE6D7;border-radius:8px;color:#232323;">#F6F0E4<br/>cream_bg</span>
  <span style="display:inline-block;width:130px;padding:12px;margin:4px;background:#EFE6D7;border:1px solid #77736B;border-radius:8px;color:#232323;">#EFE6D7<br/>cream_card</span>
  <span style="display:inline-block;width:130px;padding:12px;margin:4px;background:#243A96;border-radius:8px;color:white;">#243A96<br/>cobalt_blue</span>
  <span style="display:inline-block;width:130px;padding:12px;margin:4px;background:#232323;border-radius:8px;color:white;">#232323<br/>text_primary</span>
  <span style="display:inline-block;width:130px;padding:12px;margin:4px;background:#77736B;border-radius:8px;color:white;">#77736B<br/>gray_text</span>
</div>

<br/>

<div align="center">
  <img src="app/src/main/res/drawable-nodpi/light_happy_cat_fit.png" width="150" alt="Jasny kot" />
  <img src="app/src/main/res/drawable-nodpi/dark_happy_cat_fit.png" width="150" alt="Ciemny kot" />
  <img src="app/src/main/res/drawable-nodpi/light_silly_cat_fit.png" width="150" alt="Kot zabawny" />
  <img src="app/src/main/res/drawable-nodpi/dark_angry_cat_fit.png" width="150" alt="Kot zły" />
</div>

## 1. Opis aplikacji

DailyQuest to prosta aplikacja mobilna na Androida, stworzona jako projekt do nauki organizowania pracy. Łączy dwie podstawowe rzeczy:

- technikę typu Pomodoro, czyli pracę w krótkich sesjach skupienia,
- listę zadań to-do, czyli zapisywanie i odhaczanie rzeczy do zrobienia.

Użytkownik może dodawać zadania, ustawić im termin, oznaczać je jako wykonane i zbierać za to punkty XP. Aplikacja ma też 25-minutowy timer focus. Po zakończonej sesji zapisuje czas skupienia oraz przyznaje XP i monety.

Żeby korzystanie było przyjemniejsze, DailyQuest ma elementy grywalizacji: poziomy, serie dni, specjalne zadanie dnia, monety oraz sklep z akcesoriami do profilu.

### Technologie

| Obszar          | Rozwiązanie                               |
| --------------- | ----------------------------------------- |
| Platforma       | Android                                   |
| Język           | Java                                      |
| Baza lokalna    | SQLite przez `SQLiteOpenHelper`           |
| Dane profilu    | `SharedPreferences`                       |
| Zewnętrzne dane | `https://bored-api.appbrewery.com/random` |
| Build           | Gradle Kotlin DSL                         |

---

## 2. Opis podziału pracy

<div style="padding:14px;border:1px solid #EFE6D7;border-radius:8px;background:#F6F0E4; color:#000;">
  <b>Kierownik budowy:</b> Emilia Kościelniak
</div>

Projekt jest podzielony na kilka prostych części. Każda odpowiada za inny fragment aplikacji.

### Dane

Ta część odpowiada za przechowywanie informacji i pobieranie specjalnego zadania dnia.

| Plik                              | Rola                                                      |
| --------------------------------- | --------------------------------------------------------- |
| `data/model/Task.java`            | Model pojedynczego zadania.                               |
| `data/model/FocusSession.java`    | Model sesji skupienia.                                    |
| `data/local/DatabaseHelper.java`  | Tworzy i obsługuje bazę SQLite.                           |
| `data/local/PrefsManager.java`    | Zapisuje XP, streak, monety, akcesoria i dane profilu.    |
| `data/local/DebugSeedData.java`   | Dodaje dane testowe (aby ułatwić testowanie)              |
| `data/remote/BoredApiClient.java` | Pobiera zadanie dnia z API albo wybiera zadanie awaryjne. |

### Logika aplikacji

Ta część łączy dane z ekranami i pilnuje zasad działania aplikacji.

| Plik                                   | Rola                                                                 |
| -------------------------------------- | -------------------------------------------------------------------- |
| `data/repository/QuestRepository.java` | Główny punkt dostępu do zadań, XP, monet, streaka i historii focus.  |
| `FocusSessionManager.java`             | Zarządza timerem Pomodoro: start, pauza, wznowienie, stop i nagrody. |

### Ekrany

Ta część odpowiada za to, co widzi użytkownik.

| Plik                                    | Ekran                                           |
| --------------------------------------- | ----------------------------------------------- |
| `SplashActivity.java`                   | Ekran startowy.                                 |
| `MainActivity.java`                     | Główny kontener i dolna nawigacja.              |
| `ui/home/HomeFragment.java`             | Ekran główny z timerem, XP i zadaniami na dziś. |
| `ui/tasklist/TaskListFragment.java`     | Lista zadań.                                    |
| `ui/statistics/StatisticsFragment.java` | Statystyki.                                     |
| `ui/history/FocusHistoryFragment.java`  | Historia sesji focus.                           |
| `ui/profile/ProfileFragment.java`       | Profil, monety i sklep z akcesoriami.           |
| `ui/adapter/TaskAdapter.java`           | Wyświetlanie zadań na liście.                   |

---

## 3. Opis i diagram bazy danych

Aplikacja używa lokalnej bazy SQLite `daily_quest.db`. Baza jest bardzo prosta i ma dwie główne tabele:

- `tasks` - zadania z listy to-do oraz specjalne zadania dnia,
- `focus_sessions` - zapisane sesje pracy w skupieniu.

Oprócz SQLite aplikacja używa `SharedPreferences` o nazwie `daily_quest_prefs`. Tam trafia postęp profilu, np. XP, poziom, streak, monety, kupione akcesoria i aktualne zadanie dnia.

### Tabela `tasks`

| Kolumna        | Typ                               | Opis                                 |
| -------------- | --------------------------------- | ------------------------------------ |
| `id`           | INTEGER PRIMARY KEY AUTOINCREMENT | Id zadania.                          |
| `title`        | TEXT                              | Nazwa zadania.                       |
| `is_completed` | INTEGER DEFAULT 0                 | Czy zadanie jest wykonane.           |
| `is_api_task`  | INTEGER DEFAULT 0                 | Czy zadanie pochodzi z zadania dnia. |
| `xp_reward`    | INTEGER DEFAULT 10                | Nagroda XP za wykonanie.             |
| `created_at`   | INTEGER                           | Data utworzenia.                     |
| `completed_at` | INTEGER DEFAULT 0                 | Data wykonania.                      |
| `due_date`     | INTEGER DEFAULT 0                 | Termin zadania.                      |

### Tabela `focus_sessions`

| Kolumna           | Typ                               | Opis                                |
| ----------------- | --------------------------------- | ----------------------------------- |
| `id`              | INTEGER PRIMARY KEY AUTOINCREMENT | Id sesji.                           |
| `duration_millis` | INTEGER                           | Czas trwania sesji w milisekundach. |
| `timestamp`       | INTEGER                           | Data zapisania sesji.               |
| `coins_earned`    | INTEGER                           | Liczba zdobytych monet.             |

### Diagram bazy danych

```mermaid
erDiagram
    TASKS {
        INTEGER id PK
        TEXT title
        INTEGER is_completed
        INTEGER is_api_task
        INTEGER xp_reward
        INTEGER created_at
        INTEGER completed_at
        INTEGER due_date
    }

    FOCUS_SESSIONS {
        INTEGER id PK
        INTEGER duration_millis
        INTEGER timestamp
        INTEGER coins_earned
    }
```

Tabele nie mają relacji kluczem obcym. Zadania i sesje focus są zapisywane osobno, bo opisują dwa różne typy aktywności użytkownika.

---

## 4. Schemat nawigacji po aplikacji

Po włączeniu aplikacji użytkownik trafia najpierw na ekran startowy, a potem do głównego ekranu z dolną nawigacją.

<div align="center">
  <img src="app/src/main/res/drawable-nodpi/icon_light_fit.png" width="180" alt="DailyQuest jasne logo" />
</div>

```mermaid
flowchart TD
    A["Start aplikacji"] --> B["SplashActivity"]
    B --> C["MainActivity"]
    C --> D["Home"]
    C --> E["Tasks"]
    C --> F["Stats"]
    C --> G["History"]
    C --> H["Profile"]

    D --> D1["XP, poziom, streak, monety"]
    D --> D2["Timer Pomodoro / Focus"]
    D --> D3["Zadania na dziś"]

    E --> E1["Dodanie zadania"]
    E --> E2["Wybór terminu"]
    E --> E3["Odhaczenie zadania"]

    F --> F1["Podsumowanie postępu"]
    F --> F2["Wykres tygodnia"]

    G --> G1["Historia sesji focus"]
    G --> G2["Czas i monety"]

    H --> H1["Profil"]
    H --> H2["Akcesoria"]
    H --> H3["Reset postępu"]
```

### Dolna nawigacja

| Zakładka | Fragment               | Co można tam zrobić                                            |
| -------- | ---------------------- | -------------------------------------------------------------- |
| Home     | `HomeFragment`         | Sprawdzić postęp, włączyć Pomodoro i zobaczyć zadania na dziś. |
| Tasks    | `TaskListFragment`     | Dodać zadanie, ustawić termin i oznaczyć je jako wykonane.     |
| Stats    | `StatisticsFragment`   | Zobaczyć statystyki XP, zadań i skupienia.                     |
| History  | `FocusHistoryFragment` | Przejrzeć zapisane sesje focus.                                |
| Profile  | `ProfileFragment`      | Sprawdzić profil, monety, akcesoria i zresetować dane.         |

---
## Zrzuty ekranu aplikacji
<img width="290" height="656" alt="z1" src="https://github.com/user-attachments/assets/5e888553-ef82-44d9-9f1d-eca04ebf30aa" />
<img width="289" height="653" alt="z2" src="https://github.com/user-attachments/assets/e13ab65c-68bb-4fd7-af0a-c20b2fa045a9" />
<img width="292" height="654" alt="z3" src="https://github.com/user-attachments/assets/3b1cb30d-34d0-429e-affd-8b00a1740b17" />
<img width="283" height="651" alt="z4" src="https://github.com/user-attachments/assets/be993857-d6f5-45ed-ba8a-69bb92eacb38" />
<img width="289" height="652" alt="z5" src="https://github.com/user-attachments/assets/bdc2471e-5d3b-4721-afc6-b2fdae57d01c" />


___
<div align="center">
  <img src="app/src/main/res/drawable-nodpi/dark_silly_cat_fit.png" width="130" alt="DailyQuest cat" />
  <p><b>DailyQuest</b><br> Emilia Kościelniak</p>
</div>
