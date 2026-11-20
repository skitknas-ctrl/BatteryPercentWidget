# Battery Percent Widget

En enkel hemskärmswidget för Android som visar batterinivån i procent.

- Uppdateras vid laddning/urkoppling, låg batterinivå och omstart
- Inställningar för mätarens ljushet och opacitet, textens färg och opacitet samt storlek
- Inga behörigheter utöver `RECEIVE_BOOT_COMPLETED`
- Kräver Android 12 (API 31) eller senare

## Bygga

```
./gradlew assembleRelease
```

Widgeten läggs till via hemskärmens widgetmeny. Inställningarna nås genom att öppna appen eller genom att hålla fingret på widgeten och välja ändra.

## Utgåvor och F-Droid

- Varje push till `main` ger ett testbygge via GitHub Actions (för Obtainium).
- En officiell utgåva har fast `versionCode` och `versionName` i `app/build.gradle.kts` och en tagg `release-<versionName>`
  (nu `release-1.3.0`). F-Droid bygger från den taggen. Ny utgåva: höj båda värdena, lägg till
  `metadata/en-US/changelogs/<versionCode>.txt`, pusha och sätt taggen när bygget gått igenom.
- Texterna som visas på F-Droid ligger i `metadata/en-US/`.

## Licens

GPL-3.0-or-later. Se `LICENSE`.
