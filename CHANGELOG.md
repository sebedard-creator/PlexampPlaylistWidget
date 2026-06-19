# Changelog

Toutes les modifications notables apportées à ce projet seront documentées dans ce fichier.

Le format est basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.0.0/), et ce projet adhère à [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed
- **Compilation Kotlin :** Correction de l'erreur `Returns are not allowed for functions with expression body` dans `UrlTransformer.kt`. La fonction utilise désormais un "block body" (`{ return ... }`) pour autoriser les retours anticipés (`return null`) lors de l'extraction des paramètres de l'URL.
- **Build System :** Mise à jour de l'Android Gradle Plugin (AGP) vers la version 8.3.2 et de Gradle vers la version 8.4. Cela corrige un bug d'incompatibilité majeur avec le JDK 21 inclus dans les versions récentes d'Android Studio (erreur `ModuleTarget is malformed` avec `jlink.exe`).
- **NFC :** Correction de l'interception des tags. Le système Android prenait la main car les tags Plexamp contiennent une URI (http/https) et non un type MIME. Les filtres `IntentFilter` du *Foreground Dispatch* ont été étendus pour capturer de force les URIs et empêcher le système d'ouvrir le tag.
- **Plexamp Deep Linking :** Correction du lancement de la playlist. L'URL générée contient un paramètre `uri=server://...` dont les caractères spéciaux (`://`) embrouillaient l'intent Android. Le code utilise désormais `Uri.Builder` pour assurer un URL Encoding strict. De plus, le flag `FLAG_ACTIVITY_CLEAR_TOP` a été ajouté lors du clic sur le widget pour s'assurer que Plexamp reçoive bien l'ordre de lecture s'il tournait déjà en arrière-plan.

### Changed
- **UI / Widget :** Amélioration esthétique majeure du widget. Le texte de la playlist a été agrandi (de 14sp à 22sp) et mis en gras avec une police plus moderne (`sans-serif-medium`). Ajout de marges (`padding`) aérées. Le fond du widget adopte désormais des coins joliment arrondis (20dp) avec une opacité légèrement augmentée pour un effet de "verre fumé" plus qualitatif. Ajout d'un effet visuel au clic (Ripple effect).

### Added
- **UI / Application :** Ajout de deux nouvelles icônes (Crayon et Poubelle) à côté de chaque playlist dans l'application principale pour permettre de les renommer facilement et de les supprimer via une boîte de dialogue de confirmation sécurisée.
- **UI / Application :** Le geste de "Swipe to delete" (glisser pour supprimer), jugé trop aléatoire, a été désactivé pour éviter les suppressions accidentelles.
