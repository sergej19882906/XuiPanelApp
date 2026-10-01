# X-UI Panel Android App

Приложение для Android для доступа к панели управления **x-ui** (Xray/V2Ray proxy panel).

## Возможности

- 🌐 Доступ к веб-интерфейсу x-ui через WebView
- ⚙️ Настройка подключения (хост, порт, путь)
- 🔒 Поддержка HTTPS/HTTP
- ✅ Принятие самоподписанных SSL сертификатов
- 🔄 Быстрое обновление страницы
- 📱 Оптимизированный интерфейс для мобильных устройств

## Требования

- Android 7.0 (API 24) и выше
- Панель x-ui с доступом по сети

## Установка

### Готовый APK

Скачайте последнюю версию в [Releases](../../releases) и установите APK на устройство.

### Сборка из исходников

#### Вариант 1: Android Studio

1. Откройте проект в Android Studio
2. Дождитесь синхронизации Gradle
3. Нажмите **Run** или **Build → Build APK**

#### Вариант 2: Командная строка

```bash
cd XuiPanelApp
./gradlew assembleDebug
```

APK файл будет создан в `app/build/outputs/apk/debug/`

Подписанный release-сборка: `./gradlew assembleRelease` (требуется `keystore.properties` в корне проекта, см. ниже).

## Подпись релиза

1. Создайте keystore:
   ```bash
   keytool -genkeypair -v -keystore keystore/xui_panel.jks -alias xui_panel \
     -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Создайте файл `keystore.properties` в корне проекта:
   ```properties
   storeFile=keystore/xui_panel.jks
   storePassword=ВАШ_ПАРОЛЬ
   keyAlias=xui_panel
   keyPassword=ВАШ_ПАРОЛЬ
   ```

Файл `keystore.properties` и каталог `keystore/` добавлены в `.gitignore` и не должны попадать в репозиторий.

## Публикация релиза на GitHub

CI автоматически собирает release APK и прикрепляет его к GitHub Release при пуше тега вида `v1.0.0`:

```bash
git tag v1.0.0
git push origin v1.0.0
```

Для подписи в CI добавьте в настройках репозитория (**Settings → Secrets and variables → Actions**) следующие secrets:

| Secret | Значение |
|---|---|
| `KEYSTORE_BASE64` | Keystore в base64: `base64 -w 0 keystore/xui_panel.jks` |
| `KEYSTORE_STORE_PASSWORD` | Пароль хранилища |
| `KEYSTORE_KEY_ALIAS` | Алиас ключа (`xui_panel`) |
| `KEYSTORE_KEY_PASSWORD` | Пароль ключа |

Каждый push и pull request в `master`/`main` проверяются workflow **Build**, который собирает debug APK.

## Настройка

После запуска приложения:

1. Откройте **Настройки** (меню → Настройки)
2. Укажите параметры подключения:
   - **Хост панели** — IP-адрес или домен сервера
   - **Порт** — порт панели (по умолчанию 2053)
   - **Путь** — путь к панели (обычно `/`)
   - **HTTPS** — включите, если используется SSL

3. Нажмите **Назад** и подключитесь к панели

## Структура проекта

```
XuiPanelApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/xui/panelapp/
│   │   │   ├── MainActivity.java       # Главный экран с WebView
│   │   │   └── SettingsActivity.java   # Экран настроек
│   │   ├── res/
│   │   │   ├── layout/                 # XML макеты
│   │   │   ├── menu/                   # Меню приложения
│   │   │   ├── xml/                    # Настройки
│   │   │   └── values/                 # Строки, цвета, темы
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
└── settings.gradle
```

## Лицензия

MIT License
