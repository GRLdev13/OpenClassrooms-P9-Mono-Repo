<p align="center">
   <img src="./front/src/favicon.png" width="192px" />
</p>

# MicroCRM (P7 - Développeur Full-Stack - Java et Angular - Mettez en œuvre l'intégration et le déploiement continu d'une application Full-Stack)

MicroCRM est une application de démonstration basique ayant pour être objectif de servir de socle pour le module "P7 - Développeur Full-Stack".

L'application MicroCRM est une implémentation simplifiée d'un ["CRM" (Customer Relationship Management)](https://fr.wikipedia.org/wiki/Gestion_de_la_relation_client). Les fonctionnalités sont limitées à la création, édition et la visualisations des individus liés à des organisations.

![Page d'accueil](./misc/screenshots/screenshot_1.png)
![Édition de la fiche d'un individu](./misc/screenshots/screenshot_2.png)

## Code source

### Organisation

Ce [monorepo](https://en.wikipedia.org/wiki/Monorepo) contient les 2 composantes du projet "MicroCRM":

- La partie serveur (ou "backend"), en Java SpringBoot 3;
- La partie cliente (ou "frontend"), en Angular 17.

### Démarrer avec les sources

#### Serveur

##### Dépendances

- [OpenJDK >= 17](https://openjdk.org/)

##### Procédure

1. Se positionner dans le répertoire `back` avec une invite de commande:

   ```shell
   cd back
   ```

2. Construire le JAR:

   ```shell
   # Sur Linux
   ./gradlew build

   # Sur Windows
   gradlew.bat build
   ```

3. Démarrer le service:

   ```shell
   java -jar build/libs/microcrm-0.0.1-SNAPSHOT.jar
   ```

Puis ouvrir l'URL http://localhost:8081 dans votre navigateur.

#### Client

##### Dépendances

- [NPM >= 10.2.4](https://www.npmjs.com/)

##### Procédure

1. Se positionner dans le répertoire `front` avec une invite de commande:

   ```shell
   cd front
   ```

2. (La première fois seulement) Installer les dépendances NodeJS:

   ```shell
   npm install
   ```

3. Démarrer le service de développement:

   ```shell
   npx @angular/cli serve
   ```

Puis ouvrir l'URL http://localhost:4200 dans votre navigateur.

### Exécution des tests

#### Client

**Dépendances**

- Google Chrome ou Chromium

Dans votre terminal:

```shell
cd front
CHROME_BIN=</path/to/google/chrome> npm test
```

Pour générer les rapports de tests et de couverture, exécutez `npm test -- --watch=false --browsers=ChromeHeadless --code-coverage` dans `front`. Le rapport JUnit se trouve dans `front/test-results/junit.xml` et les rapports de couverture HTML et Cobertura dans `front/coverage/microcrm/`. Le workflow **Project tests** affiche les résultats frontend dans le résumé de la CI et publie ces fichiers comme artefacts `frontend-test-results` et `frontend-coverage-report`.

#### Serveur

Dans votre terminal:

```shell
cd back
./gradlew test
```

Le rapport de couverture JaCoCo (HTML et XML) est généré dans `back/build/reports/jacoco/test/`. Dans GitHub Actions, il est téléchargeable depuis l'artefact `backend-jacoco-report` du workflow **Project tests**.

### Analyse de sécurité avec CodeQL

Après la réussite des tests du workflow [Project tests](.github/workflows/test-runner.yml), [CodeQL Advanced](.github/workflows/codeql-analysis.yml) analyse le backend Java et le frontend TypeScript pour les pull requests internes vers `master`, les pushs sur `master` et chaque samedi à 12 h 45 UTC. Un lancement manuel de **Project tests** lance aussi l'analyse après les tests.

Pour l'activer, ajoutez ce workflow à la branche par défaut du dépôt et vérifiez que GitHub Actions est activé. Le dépôt doit être public ou disposer de GitHub Code Security. Si la configuration CodeQL par défaut est déjà activée, passez à la configuration avancée dans **Settings > Advanced Security > CodeQL analysis** afin d'utiliser ce workflow. Les résultats sont visibles dans **Security > Code scanning** après la première analyse réussie.

Le workflow utilise la suite CodeQL `security-and-quality` pour les trois langages analysés (GitHub Actions, Java et TypeScript). Elle ajoute aux requêtes de sécurité étendues des contrôles de fiabilité et de maintenabilité. Elle peut remonter davantage de faux positifs ; il faut examiner les alertes avant de les classer ou de modifier le code.

Pour empêcher la fusion d'une pull request qui introduit une alerte grave, créez dans GitHub **Settings > Rules > Rulesets** une règle de branche visant `master`, activez **Require code scanning results**, choisissez **CodeQL** et réglez **Security alerts** sur **High or higher** (ou **Critical**) et **Alerts** sur **Errors**. La seule exécution de CodeQL ne rend pas ses alertes bloquantes.

CodeQL ne fournit pas de taux de duplication, de seuil de complexité cyclomatique ni de pourcentage de couverture de tests pour ce projet. Pour ces critères, utilisez des contrôles séparés dans la CI : PMD CPD sur `back/src/main/java` et `front/src` pour les duplications, les règles PMD `CyclomaticComplexity`/`CognitiveComplexity` pour Java et ESLint `complexity` pour TypeScript. Les rapports JaCoCo et Karma sont déjà générés ; des seuils bloquants peuvent être ajoutés avec `jacocoTestCoverageVerification` dans `back/build.gradle` (à relier à `check` ou au job de tests) et `coverageReporter.check.global` dans `front/karma.conf.js`. Définissez ces seuils après avoir vérifié les valeurs des rapports actuels et ajouté les tests manquants, afin de ne pas faire échouer la CI dès leur activation.

### Images Docker

La construction et la publication des images Docker sont temporairement désactivées dans le workflow **Project tests**. Les tests et CodeQL continuent de s'exécuter. Le fichier [Build Docker images](.github/workflows/docker-build.yml) conserve la configuration à réactiver ultérieurement.

#### Démarrer avec Docker Compose

Depuis la racine du dépôt, construire et démarrer le client et le serveur dans deux conteneurs :

```shell
docker compose up --build
```

Le client est disponible sur https://localhost et l'API sur http://localhost:8081. Pour arrêter les conteneurs, exécuter `docker compose down`.

#### Envoyer les logs Docker vers ELK

Le service Filebeat de `elk/compose.yml` détecte les conteneurs `front` et `back` grâce à leurs labels Docker. Il lit leurs logs standard, les transmet à Logstash sur le port 5044, puis Logstash les indexe dans Elasticsearch sous `app-logs-*`. Caddy journalise les requêtes HTTP du frontend ; le backend journalise les requêtes HTTP avec leur méthode, leur chemin, leur statut et leur durée. Les messages de la console JavaScript du navigateur ne sont pas des logs du conteneur `front`.

Depuis la racine du dépôt, lancer ou actualiser les deux projets Compose :

```shell
docker compose -f elk/compose.yml up -d
docker compose up -d --build front back
```

Générer quelques requêtes sur l'application, puis ouvrir Kibana sur http://localhost:5601. Dans Discover, sélectionner une vue de données qui cible `app-logs-*` (ou la créer avec `@timestamp` comme champ temporel), choisir une période qui inclut les requêtes récentes, puis filtrer sur `service.name` (`front` ou `back`). Une vue sur les données d'exemple de Kibana ou une période trop courte n'affichera pas ces logs. Filebeat lit les fichiers de logs Docker du moteur hôte ; cette configuration suppose le pilote Docker `json-file` et un moteur Docker Linux, comme les conteneurs Linux de Docker Desktop.

##### Tracer les routes HTTP dans Kibana

Chaque requête HTTP produit désormais un événement `http.access`. Logstash remplace son champ `message` par une trace courte directement lisible dans la colonne par défaut de Discover :

```text
GET /persons/{id} -> 200
POST /organizations -> 201
GET /route-inconnue -> 404
```

La trace contient la méthode HTTP, la route appelée et le code de réponse. Pour le backend, le filtre HTTP récupère le modèle de route reconnu par Spring après le traitement de la requête. Les appels `/persons/1` et `/persons/2` peuvent ainsi être regroupés sous une même route telle que `/persons/{id}`. L'URL concrète reste conservée séparément. Pour le frontend servi par Caddy, qui ne connaît pas les modèles de routes Angular, l'URL appelée est utilisée comme route.

Les principaux champs indexés sont :

| Champ | Contenu |
| --- | --- |
| `message` | Trace lisible, par exemple `GET /persons/{id} -> 200` |
| `service.name` | Service ayant traité la requête : `front` ou `back` |
| `http.route` | Modèle de route Spring, ou URL appelée pour le frontend |
| `url.original` | URL réellement demandée, avec ses paramètres concrets |
| `http.request.method` | Méthode HTTP (`GET`, `POST`, `PUT`, `DELETE`, etc.) |
| `http.response.status_code` | Code de réponse HTTP |
| `http_access.duration_ms` | Durée totale de la requête en millisecondes |
| `event.duration` | Durée totale au format ECS, en nanosecondes |

Dans Discover, sélectionner la vue de données `app-logs-*`, puis utiliser le filtre KQL suivant :

```text
event.dataset: "http.access"
```

La colonne `message` suffit pour lire les appels. Pour une vue plus détaillée, ajouter les colonnes `@timestamp`, `service.name`, `http.route`, `url.original`, `http.response.status_code` et `http_access.duration_ms`.

Afficher uniquement les erreurs HTTP :

```text
event.dataset: "http.access" and http.response.status_code >= 400
```

Afficher les appels du backend sur une route donnée :

```text
event.dataset: "http.access" and service.name: "back" and http.route: "/persons/{id}"
```

Afficher les requêtes HTTPS reçues par le frontend :

```text
event.dataset: "http.access" and service.name: "front" and http_access.request.tls.version: *
```

Les événements backend exposent également `http_access.metrics.thread_cpu_ms` (temps CPU du thread), `http_access.metrics.heap_used_bytes` (mémoire JVM après la requête) et `http_access.metrics.heap_delta_bytes` (variation pendant la requête). Le temps CPU exclut les traitements asynchrones exécutés sur d'autres threads. Les mesures de mémoire concernent toute la JVM et peuvent donc varier à cause d'autres requêtes ou du ramasse-miettes.

Après une modification du filtre ou du pipeline, reconstruire le backend et recréer Logstash :

```shell
docker compose up -d --build back
docker compose -f elk/compose.yml up -d --force-recreate logstash
```

Générer ensuite de nouvelles requêtes. Les anciens documents Elasticsearch ne sont pas transformés rétroactivement. Si `http.route` ou les autres nouveaux champs ne sont pas proposés dans Kibana, actualiser la liste des champs de la vue de données `app-logs-*` dans **Stack Management > Data Views**.

#### Client

##### Construire l'image

```shell
docker build --target front -t orion-microcrm-front:latest .
```

##### Exécuter l'image

```shell
docker run -it --rm -p 80:80 -p 443:443 orion-microcrm-front:latest
```

L'application sera disponible sur https://localhost.

#### Serveur

##### Construire l'image

```shell
docker build --target back -t orion-microcrm-back:latest .
```

##### Exécuter l'image

```shell
docker run -it --rm -p 8081:8081 orion-microcrm-back:latest
```

L'API sera disponible sur http://localhost:8081.
