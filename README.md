# Movies App

Movies App - учебное веб-приложение для ведения коллекции фильмов. В нем можно хранить фильмы, людей из съемочной команды и локации, фильтровать и сортировать таблицу фильмов, импортировать пачки записей из YAML и смотреть небольшую аналитику по базе.

Проект написан как обычное Spring Boot приложение: backend отдает и HTML-страницы на Thymeleaf, и JSON API, а изменения в таблице фильмов дополнительно рассылаются через WebSocket.

## Что умеет приложение

- CRUD для фильмов, персон и локаций.
- Таблица фильмов с пагинацией, фильтрами и сортировкой по названию, режиссеру, сценаристу, оператору, жанру и MPAA.
- Проверка уникальности фильма по комбинации `screenwriter + name + genre`.
- YAML-импорт фильмов с созданием вложенных персон и локаций.
- История импортов: обычный пользователь видит свои операции, администратор может включить просмотр всех.
- Аналитика через PostgreSQL-функции:
  - количество фильмов с жанром больше выбранного;
  - поиск фильмов по подстроке в названии;
  - список фильмов с жанром больше выбранного;
  - фильмы без "Оскара";
  - сценаристы, у которых нет фильмов с "Оскаром".
- Live-обновление страницы фильмов через STOMP/WebSocket `/ws` и топик `/topic/movies`.

## Стек

- Java 17
- Spring Boot 3.5.7
- Spring MVC, Validation, Security, WebSocket
- Thymeleaf
- Spring Data JPA c EclipseLink вместо Hibernate
- PostgreSQL
- Flyway как зависимость проекта
- Jackson YAML для импорта
- JUnit 5, AssertJ, Mockito

## Быстрый старт

Нужны Java 17, Docker для тестов с Testcontainers, PostgreSQL для локального запуска и Maven Wrapper из репозитория. На Windows команды ниже можно запускать из PowerShell.

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

По умолчанию приложение стартует на:

```text
http://localhost:8082
```

Основные страницы:

- `http://localhost:8082/movies`
- `http://localhost:8082/persons`
- `http://localhost:8082/locations`
- `http://localhost:8082/analytics`

Корневой маршрут `/` отдельно не настроен, поэтому удобнее сразу открывать `/movies`.

## Вход

Почти все маршруты закрыты Spring Security form-login. Тестовые пользователи заведены в памяти:

| Логин | Пароль | Роль |
| --- | --- | --- |
| `admin` | `admin` | `ADMIN` |
| `user` | `user` | `USER` |

`ADMIN` отличается тем, что может смотреть историю импортов всех пользователей.

## База данных

Базовая конфигурация лежит в `src/main/resources/application.properties`. Секреты не хранятся в репозитории: реальные параметры подключения передаются через переменные окружения или через локальный файл `application-local.properties` в корне проекта либо `src/main/resources/application-local.properties`. Оба варианта игнорируются git и автоматически подхватываются при запуске.

По умолчанию приложение ожидает локальную PostgreSQL здесь:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/movies
spring.datasource.username=movies
spring.datasource.password=movies
server.port=8082
spring.flyway.enabled=true
```

Если у вас другая база, удобнее переопределить настройки при запуске:

```powershell
.\mvnw.cmd spring-boot:run `
  "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:postgresql://localhost:5432/movies --spring.datasource.username=postgres --spring.datasource.password=postgres"
```

Или через переменные окружения:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/movies"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"
.\mvnw.cmd spring-boot:run
```

Схема БД описана в SQL-файлах:

- `src/main/resources/db/migration/V1__init.sql` - enum-типы, таблицы `locations`, `persons`, `movies`, индексы;
- `src/main/resources/db/migration/V2__import_operations.sql` - таблица истории импортов;
- `src/main/resources/db/migration/V3__functions.sql` - функции для аналитики;
- `src/main/resources/db/migration/V4__movie_uniqueness.sql` - уникальный индекс `screenwriter + lower(name) + genre`.

Flyway включен по умолчанию и применяет миграции при старте приложения.

## Модель данных

Основные сущности:

- `Movie` - фильм с координатами, бюджетом, сборами, жанром, рейтингом MPAA, количеством "Оскаров", "Золотых пальм" и ссылками на участников съемочной команды.
- `Person` - человек с именем, весом, гражданством, цветом глаз/волос и необязательной локацией.
- `Location` - именованная точка с координатами.
- `ImportOperation` - запись об одной попытке YAML-импорта.

Справочники:

- жанры: `WESTERN`, `DRAMA`, `COMEDY`, `MUSICAL`, `FANTASY`;
- MPAA: `G`, `PG`, `PG_13`, `R`, `NC_17`;
- цвета: `GREEN`, `BLUE`, `YELLOW`, `ORANGE`, `WHITE`;
- страны: `GERMANY`, `SPAIN`, `THAILAND`.

Важные ограничения:

- `Movie.name` не должен быть пустым;
- `Movie.coordinates.x` должен быть больше `-924`;
- `budget`, `totalBoxOffice`, `goldenPalmCount`, `length`, `oscarsCount` - положительные, если поле заполнено;
- у фильма обязательно должен быть сценарист и жанр;
- у одного сценариста не может быть двух фильмов с одинаковыми `name` и `genre`;
- `Person.weight` должен быть положительным;
- `Location.name` обязателен и не длиннее 501 символа.

## REST API

Все эндпоинты требуют аутентификации.

### Фильмы

| Метод | Путь | Что делает |
| --- | --- | --- |
| `GET` | `/api/movies` | Список фильмов с фильтрами, сортировкой и пагинацией |
| `GET` | `/api/movies/{id}` | Детальная карточка фильма |
| `GET` | `/api/movies/{id}/form` | Данные фильма в формате формы |
| `POST` | `/api/movies` | Создать фильм |
| `PUT` | `/api/movies/{id}` | Обновить фильм |
| `DELETE` | `/api/movies/{id}` | Удалить фильм |

Параметры списка: `page`, `size`, `sort`, `direction`, `name`, `directorName`, `screenwriterName`, `operatorName`, `genre`, `mpaaRating`.

### Персоны и локации

| Метод | Путь | Что делает |
| --- | --- | --- |
| `GET` | `/api/persons` | Список персон |
| `GET` | `/api/persons/{id}` | Данные персоны для формы |
| `GET` | `/api/persons/{id}/details` | Детальная карточка персоны |
| `POST` | `/api/persons` | Создать персону |
| `PUT` | `/api/persons/{id}` | Обновить персону |
| `DELETE` | `/api/persons/{id}` | Удалить персону |
| `GET` | `/api/locations` | Список локаций |
| `GET` | `/api/locations/{id}` | Одна локация |
| `POST` | `/api/locations` | Создать локацию |
| `PUT` | `/api/locations/{id}` | Обновить локацию |
| `DELETE` | `/api/locations/{id}` | Удалить локацию |

При удалении персоны можно передать тело с переназначением ролей. Если тело не передано, сервис использует пустой `PersonReassignmentDto`.

### Импорт

| Метод | Путь | Что делает |
| --- | --- | --- |
| `POST` | `/api/movies/import` | Импортировать YAML-файл, поле multipart-формы называется `file` |
| `GET` | `/api/movies/import/history` | История импортов текущего пользователя |

Параметры истории: `page`, `size`, `all`. Параметр `all=true` работает только для пользователя с ролью `ADMIN`.

Пример YAML:

```yaml
movies:
  - name: "Autumn Lights"
    coordinates:
      x: 48.7
      y: 320
    oscarsCount: 1
    budget: 4500000
    totalBoxOffice: 12.4
    mpaaRating: "PG_13"
    director:
      data:
        name: "Mila North"
        weight: 60.5
        nationality: "GERMANY"
    screenwriter:
      data:
        name: "Egor Khazin"
        weight: 72.3
        nationality: "SPAIN"
        location:
          name: "Madrid Hub"
          x: 12.4
          y: 45.0
    length: 118
    goldenPalmCount: 2
    genre: "DRAMA"
```

Готовые примеры лежат в `samples/import`.

### Аналитика

| Метод | Путь | Что делает |
| --- | --- | --- |
| `GET` | `/api/analytics/genre-count?genre=DRAMA` | Количество фильмов с жанром больше указанного |
| `GET` | `/api/analytics/name-search?substring=star` | Поиск по подстроке в названии |
| `GET` | `/api/analytics/genre-list?genre=DRAMA` | Фильмы с жанром больше указанного |
| `GET` | `/api/analytics/no-oscars` | Фильмы без "Оскара" |
| `GET` | `/api/analytics/screenwriters-no-oscars` | Сценаристы без фильмов с "Оскаром" |

## Тесты

Запуск:

```powershell
.\mvnw.cmd test
```

В тестах сейчас есть проверка загрузки контекста Spring Boot, конвертеров PostgreSQL enum-типов и валидатора уникальности фильмов. Тест контекста поднимает PostgreSQL через Testcontainers и прогоняет Flyway-миграции. Если Docker недоступен, этот интеграционный тест пропускается.

## Полезные файлы

- `src/main/java/ru/ifmo/movies_app/web` - MVC и REST-контроллеры.
- `src/main/java/ru/ifmo/movies_app/service` - бизнес-логика.
- `src/main/java/ru/ifmo/movies_app/repository` - JPA/EclipseLink доступ к данным.
- `src/main/resources/templates` - Thymeleaf-страницы.
- `src/main/resources/static` - CSS и клиентский JavaScript.
- `src/main/resources/db/migration` - SQL для схемы и функций.
- `samples/import` - YAML-файлы для ручной проверки импорта.
