# ![Gatling](http://gatling.io/images/gatling-logo.png)

##Synopsis

Ce script en scala/DSL injecte 100 000 valeurs aléatoires et de façon concurrente vers un service REST par le biais de 10 injecteurs.
Il vérifie que les valeurs injectées sont correcte en solicitant le service de vérification de la synthèse.
Il sera utilisé pour départager l'ensemble des participants du concours.

## Téléchargement

[Lien de téléchargement deGatling](http://gatling.io/#/download)

##Installation

Le script a besoin de certaines librairies pour pouvoir envoyer les résultats des tests:

- Téléchargez les fichiers jar de HttpClient 4.5.2 [HttpComponents,HttpClient 4.5.2](https://hc.apache.org/downloads.cgi)
- Ajoutez les au dossier lib de votre gatling.
- Déposez le fichier gatling_script.scala dans le répertoire "\user-files\simulations"

##Mode d'emploi

- Exécutez le script gatling.bat sur Windows ou gatling.sh sur Linux.
- Sélectionnez la classe 'Injections' en tapant son numéro.
- Si vous souhaitez lancer le script de votre shell sur Windows : gatling.bat -s Injections (gatling.sh -s Injections)

NB : N'oublier pas d'ajouter le nom de votre équipe (TeamName) et de votre site de rattachement (TeamLocation) sur le script avant de le compiler/executer le scénario d'injection.

##Résultats

L'ensemble des résultats produits sont transmis automatiquement au serveur : http://iot-contest.corp.capgemini.com

##Equipe

- [Benabachir Issam](https://github.com/IsBena)
- [Pavageau Stanislas](https://github.com/StanislasCapgemini)
- [Ludovic Toinel](https://github.com/ltoinel)



