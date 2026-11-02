# Idle Lodge / Idle Loge

**Confiez l'ouvrage à votre personnage. Fermez l'app. Revenez récolter la Lumière.**

Un jeu d'idle hors ligne pour Android sur le thème de la franc-maçonnerie : partez de la pierre brute,
progressez d'Apprenti à Compagnon puis Maître, et bâtissez le Temple. Sans connexion, sans compte, sans publicité.
Disponible en français et en anglais.

## Principe

Choisissez un travail (taille de la pierre, fonderie, agapes, symbolique…) ou une épreuve, lancez une session,
puis posez votre téléphone. Le travail continue jusqu'à une heure, application fermée. Revenez quand vous voulez
pour récolter l'expérience et les matériaux.

- **23 compétences** : Carrière, Bûcheronnage, Pêche, Culture, Bienfaisance, Fonderie, Agapes, Écriture,
  Orfèvrerie, Illumination, Symbolique, Alchimie, Architecture, Réflexion, Négoce, Rituel, Vigilance,
  Rigueur, Force, Tempérance, Éloquence, Sagesse, Vitalité.
- **Épreuves** : affrontez vos vices et les Ténèbres (l'Envie, le Doute, le Fanatisme, les trois Mauvais Compagnons…)
  dans 37 lieux d'épreuve, au Maillet, par l'Éloquence ou par les Symboles.
- **Loges**, **Cabinet de réflexion**, **Salle humide**, **Échelle de Jacob**, **Pèlerinages**, **Foire solsticiale**,
  **Demeure** personnelle, **Élévation** (prestige) à 99, et bien plus.

Le vocabulaire complet du jeu est décrit dans [GLOSSAIRE.md](GLOSSAIRE.md).

## Origine du projet

Idle Lodge est un fork re-thématisé d'**[Idle Fantasy](https://github.com/tristinbaker/IdleFantasy)** de Tristin Baker
et de ses contributeurs. Toute la mécanique de jeu vient de ce projet ; ce fork en change l'univers, les textes et le nom.

## Pour les développeurs

**Langage :** Kotlin · **UI :** Jetpack Compose + Material 3 · **Base de données :** Room ·
**Tâches de fond :** AlarmManager · **JSON :** kotlinx.serialization · **Architecture :** MVVM + Repository ·
**Injection :** Hilt · **Localisation :** ressources Android (`values/` = anglais, `values-fr/` = français)

Les textes affichés sont tous dans `app/src/main/res/values*/strings*.xml` ; les fichiers JSON de
`app/src/main/assets/data/` ne contiennent que des clés internes et les valeurs de jeu.
`tools/check_strings.py` vérifie qu'un fichier de chaînes modifié garde les mêmes clés et les mêmes paramètres de format.

`tools/make_art.py` génère le pixel art maçonnique : les icônes de compétences (`res/drawable/skill_*.png`), les symboles des bannières de saison (à partir de `tools/art_src/`) et le tablier du personnage. Il nécessite Pillow (`pip install pillow`).

### Compiler

Prérequis : Android Studio, **JDK 17** (Gradle 8.14 ne fonctionne pas avec Java 25), Android SDK 35.

```bash
./gradlew :app:assembleDebug
```

L'APK de debug se trouve dans `app/build/outputs/apk/debug/app-debug.apk`.

## Licence

GPL-3.0, comme le projet d'origine. Voir [LICENSE](LICENSE).
