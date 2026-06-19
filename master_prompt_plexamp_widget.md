# CONTEXTE ET RÔLE
Agis en tant que développeur Android Senior expert en Kotlin, Jetpack Compose (ou XML si plus adapté pour le widget), Room Database, et intégration matérielle (NFC).
Ton objectif est de générer le code complet et prêt à être intégré dans Android Studio (Studio Bot / Gemini) pour une application Android nommée "Plexamp Widget Creator".

# OBJECTIF DE L'APPLICATION
L'application permet d'intercepter un tag NFC contenant une URL de partage Plexamp (qui cause par défaut une erreur 404), d'en extraire les données pour forger une nouvelle URL fonctionnelle forçant le mode aléatoire (shuffle), et de stocker cette URL avec un nom personnalisé. Ces liens sont ensuite affichés dans un AppWidget (4x2) sous forme de liste déroulante minimaliste pour un lancement rapide.

# SPÉCIFICATIONS TECHNIQUES DÉTAILLÉES

## 1. Base de données locale (Room)
* **Entité `PlaylistEntity` :**
  * `id` (PrimaryKey, AutoGenerate)
  * `name` (String) : Nom donné par l'utilisateur.
  * `modifiedUrl` (String) : L'URL forgée par l'application.
  * `sortOrder` (Int) : Pour gérer la réorganisation "hold to reorder".
* **DAO :** Opérations CRUD complètes + mise à jour en lot pour le `sortOrder`.

## 2. Logique de Transformation d'URL
* **Entrée (Issue du scan NFC NDEF Record) :** Format type : `https://listen.plex.tv/com.plexapp.agents.none:/[UUID]?source=[MACHINE_ID]&key=[METADATA_KEY]&...`
  *Exemple :* `https://listen.plex.tv/com.plexapp.agents.none:/[UUID_PLEX]?source=[MACHINE_ID_PLEX]&key=%2Fplaylists%2F198850%2Fitems...`
* **Extraction :** Récupérer la valeur de `source` (ex: 616ac...) et la valeur de `key` (décoder l'URL encoding, ex: `/playlists/198850/items`). **Attention**, Plexamp s'attend au `key` de base, s'il y a `/items` à la fin, on le garde ou on l'utilise tel quel selon la structure de Plex. Supposons la concaténation de `/com.plexapp.plugins.library` + `[KEY_DÉCODÉ]`.
* **Sortie Forgée :** Format : `https://listen.plex.tv/player/playback/playMedia?uri=server://[MACHINE_ID]/com.plexapp.plugins.library[METADATA_KEY]&shuffle=1`
  *Exemple :* `https://listen.plex.tv/player/playback/playMedia?uri=server://[MACHINE_ID_PLEX]/com.plexapp.plugins.library/playlists/198850&shuffle=1`

## 3. L'Application Principale (UI & NFC)
* **MainActivity :** Affiche la liste des `PlaylistEntity` via un `RecyclerView`.
* **Interaction Liste :** Uniquement du texte. Intégrer un `ItemTouchHelper` permettant le glisser-déposer (Hold to Reorder). La modification de l'ordre doit mettre à jour le `sortOrder` dans la DB. Possibilité de glisser pour supprimer (Swipe to delete) ou un bouton delete textuel.
* **Ajout (FAB) :** Un clic sur le Floating Action Button ouvre une modale/dialogue affichant "Veuillez scanner le tag NFC Plexamp".
* **Interception NFC :** Pendant que la modale est active, utiliser `NfcAdapter.enableForegroundDispatch` pour intercepter exclusivement les tags NDEF. Une fois scanné, lire l'URI, exécuter la transformation d'URL expliquée au point 2, fermer la modale et ouvrir une seconde modale demandant "Entrez le nom de la playlist". Sauvegarder dans Room.

## 4. L'AppWidget (Le lanceur)
* **Spécifications AppWidgetProvider :** Taille cible 4x2.
* **Layout Widget :** * Arrière-plan : Noir translucide (`#80000000`).
  * Contenu : Une `ListView` transparente. Pas d'icônes, juste le `name` en texte blanc (lisible).
* **RemoteViewsService & Factory :** Connecter la `ListView` à la base de données Room (lire trié par `sortOrder`).
* **Intention de clic (PendingIntent) :**
  * Configurer un `PendingIntentTemplate` sur la `ListView`.
  * Au clic sur une ligne, l'application doit lancer un `Intent(Intent.ACTION_VIEW, Uri.parse(modifiedUrl))`.
  * **Flags obligatoires :** `Intent.FLAG_ACTIVITY_NEW_TASK`.
  * **Feedback :** Afficher un `Toast` : "Lancement de [Nom de la playlist]..." lors du clic

# LIVRABLES ATTENDUS DE TA PART
Génère le code modulaire et structuré. Fournis :
1. Le code des entités Room et du Database Builder.
2. Le code de la logique de transformation d'URL (Regex ou Uri.parse).
3. Le code de l'Activity pour le Foreground Dispatch NFC et le `ItemTouchHelper`.
4. L'implémentation complète du Widget (`AppWidgetProvider`, `RemoteViewsService`, Layouts XML).
5. Le `AndroidManifest.xml` (avec les permissions NFC et les déclarations du widget).

Ne suggère pas de preview, d'export de base de données ni d'icônes. Reste strictement aligné sur un design textuel et minimaliste.
