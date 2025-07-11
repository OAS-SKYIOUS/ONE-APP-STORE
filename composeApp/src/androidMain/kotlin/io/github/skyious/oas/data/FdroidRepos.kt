package io.github.skyious.oas.data

import io.github.skyious.oas.data.model.FDroidRepo

// FdroidRepos.kt
//object FdroidRepos {
   // val ALL: List<String> = listOf(
       // "https://f-droid.org/repo",
//       "https://f-droid.org/repo/index-v1.jar",
       /* "https://f-droid.org/archive",
        "https://guardianproject.info/fdroid/repo",
        "https://guardianproject.info/fdroid/archive",
        "https://apt.izzysoft.de/fdroid/repo",
        "https://microg.org/fdroid/repo",
        "https://repo.netsyms.com/fdroid/repo",
        "https://fdroid.bromite.org/fdroid/repo",
        "https://molly.im/fdroid/foss/fdroid/repo",
        "https://archive.newpipe.net/fdroid/repo",
        "https://www.collaboraoffice.com/downloads/fdroid/repo",
        "https://fdroid.libretro.com/repo",
        "https://cdn.kde.org/android/stable-releases/fdroid/repo",
        "https://cdn.kde.org/android/fdroid/repo",
        "https://calyxos.gitlab.io/calyx-fdroid-repo/fdroid/repo",
        "https://fdroid.fedilab.app/repo",
        "https://store.nethunter.com/repo",
        "https://store.nethunter.com/archive",
        "https://secfirst.org/fdroid/repo",
        "https://thecapslock.gitlab.io/fdroid-patched-apps/fdroid/repo",
        "https://mobileapp.bitwarden.com/fdroid/repo",
        "https://briarproject.org/fdroid/repo",
        "https://briarproject.org/fdroid/archive",
        "https://guardianproject-wind.s3.amazonaws.com/fdroid/repo",
        "https://nanolx.org/fdroid/repo",
        "https://fdroid.metatransapps.com/fdroid/repo",
        "https://static.cryptomator.org/android/fdroid/repo",
        "https://fdroid.twinhelix.com/fdroid/repo",
        "https://fdroid.revolt.chat/repo",
        "https://bubu1.eu/fdroidclassic/fdroid/repo",
        "https://fdroid.stackwallet.com",
        "https://fdroid.typeblog.net/",
        "https://kaffeemitkoffein.de/fdroid/repo",
        "https://releases.nailyk.fr/repo",
        "https://pili.qi0.de/fdroid/repo",
        "https://raw.githubusercontent.com/xarantolus/fdroid/main/fdroid/repo",
        "https://submarine.strangled.net/fdroid/repo",
        "https://repo.mobilsicher.de/fdroid/repo",
        "https://repo.kuschku.de/repo",
        "https://jak-linux.org/fdroid/repo",
        "https://haagch.frickel.club/files/fdroid/repo",
        "https://freeyourgadget.codeberg.page/fdroid/repo",
        "https://fdroid.funkwhale.audio/",
        "https://www.ma-nic.de/fdroid/repo",
        "https://www.ma-nic.de/fdroid/archive",
        "https://fdroid.beocode.eu/fdroid/repo",
        "https://grobox.de/fdroid/repo",
        "https://jhass.github.io/insporation/fdroid/repo",
        "https://cdimage.debian.org/mirror/divestos.org/divestos-apks/official/fdroid/archive",
        "https://cdimage.debian.org/mirror/divestos.org/divestos-apks/official/fdroid/repo",
        "https://cdimage.debian.org/mirror/divestos.org/divestos-apks/unofficial/fdroid/repo",
        "https://bubu1.eu/cctg/fdroid/repo",
        "https://fdroid.mm20.de/repo",
        "https://fdroid.ltheinrich.de/",
        "https://j2ghz.github.io/repo",
        "https://juwelierkassa.at/fdroid/repo",
        "https://skyjake.github.io/fdroid/repo",
        "https://repo.librechurch.org/fdroid",
        "https://f.lubl.de/repo",
        "https://molly.im/fdroid/repo",
        "https://raw.githubusercontent.com/nucleus-ffm/Nucleus-F-Droid-Repo/master/fdroid/repo",
        "https://raw.githubusercontent.com/rafaelvenancio98/fdroid/master/fdroid/repo",
        "https://obfusk.dev/fdroid/repo",
        "https://fdroid.partidopirata.com.ar/fdroid/repo",
        "https://redreader.org/fdroid/repo",
        "https://thedoc.eu.org/fdroid/repo",
        "https://fdroid.getsession.org/fdroid/repo",
        "https://codeberg.org/silkevicious/apkrepo/raw/branch/master/fdroid/repo",
        "https://raw.githubusercontent.com/simlar/simlar-fdroid-repo/master/fdroid/repo",
        "https://fdroid.feministwiki.org/fdroid/repo",
        "https://s2.spiritcroc.de/fdroid/repo",
        "https://s2.spiritcroc.de/testing/fdroid/repo",
        "https://unofficial-protonmail-repository.gitlab.io/unofficial-protonmail-repository/fdroid/repo",
        "https://fdroid.videlibri.de/repo",
        "https://fdroid.woz.ch/fdroid/repo",
        "https://codeberg.org/florian-obernberger/fdroid-repo/raw/branch/main/repo",
        "https://update.invizbox.com/fdroid/repo",
        "https://fdroid.storchp.de/fdroid/repo",
        "https://gitjournal.io/fdroid/repo",
        "http://fdroid.frostnerd.com/",
        "http://fdroidarchive.frostnerd.com",
        "https://raw.githubusercontent.com/2br-2b/Fdroid-repo/master/fdroid/repo",
        "https://depau.github.io/fdroid_shizuku_privileged_extension/fdroid/repo",
        "https://f-droid.monerujo.io/fdroid/repo",
        "https://github.com/onionshare/onionshare-android-nightly/raw/master/fdroid/repo",
        "https://fdroid.krombel.de/element-dev-fdroid/fdroid/repo",
        "https://fdroid.krombel.de/element-dev-gplay/fdroid/repo",
        "https://fdroid.krombel.de/riot-dev-fdroid/fdroid/repo",
        "https://eyedeekay.github.io/fdroid/repo",
        "https://dev.sum7.eu/sum7/Conversations-nightly/raw/master/fdroid/repo",
        "https://lucaapp.gitlab.io/fdroid-repository/fdroid/repo",
        "https://fdroid.cakelabs.com/",
        "https://raw.githubusercontent.com/iodeOS/fdroid/master/fdroid/repo",
        "https://ltt.rs/fdroid/repo",
        "https://fdroid.cgeo.org/",
        "https://fdroid.cgeo.org/legacy",
        "https://fdroid.cgeo.org/nightly",
        "https://fdroid.aniyomi.org/",
        "https://repo.the-sauna.icu/repo",
        "https://codeberg.org/timedin/fdroid-repo/raw/branch/main/repo",
        "https://raw.githubusercontent.com/chrisgch/tca/master/fdroid/repo",
        "https://raw.githubusercontent.com/chrisgch/tcabeta/master/fdroid/repo",
        "https://zimbelstern.eu/fdroid/repo",
        "https://raw.githubusercontent.com/efreak/auto-daily-fdroid/main/fdroid/repo",
        "https://raw.githubusercontent.com/Five-Prayers/fdroid-repo-stable/main/fdroid/repo",
        "https://iitc.app/fdroid/repo",
        "https://julianfairfax.gitlab.io/fdroid-repo/fdroid/repo",
        "https://repo.nononsenseapps.com/fdroid/repo",
        "https://fdroid.novy.software/repo",
        "https://ouchadam.github.io/fdroid-repository/repo",
        "https://raw.githubusercontent.com/jackbonadies/seekerandroid/fdroid/fdroid/repo",
        "https://git.hush.is/jahway603/sda-fdroid/raw/branch/master/fdroid/repo",
        "https://raw.githubusercontent.com/Eastcoast-Laboratories/FDroid-Repository/master/fdroid/repo",
        "https://raw.githubusercontent.com/malnvenshorn/fdroid-repository/master/fdroid/repo",
        "https://raw.githubusercontent.com/nilscc/fdroid/master/fdroid/repo",
        "https://raw.githubusercontent.com/parnikkapore/fdroid/master/fdroid/repo",
        "https://raw.githubusercontent.com/lksmasin/fdroidrepo/main/fdroid/repo",
        "https://raw.githubusercontent.com/mkg20001/zeronet-fdroid/master/fdroid/repo",
        "https://raw.githubusercontent.com/kbitGit/MangaVolumeTrackerRepo/master/fdroid/repo",
        "https://raw.githubusercontent.com/Ehviewer-Overhauled/fdroid-repo/master/fdroid/repo",
        "https://breezy-weather.github.io/fdroid-repo/fdroid/repo",
        "https://raw.githubusercontent.com/noobmaster1112/fdroid-repo/master/fdroid/repo",
        "https://raw.githubusercontent.com/madushan1000/fdroid-repo/master/repo",
        "https://raw.githubusercontent.com/andrekir/fdroid/main/repo",
        "https://raw.githubusercontent.com/candyman1/fdroidRepo1/master/repo",
        "https://raw.githubusercontent.com/lucasew/fdroid-repo/master/fdroid/repo",
        "https://raw.githubusercontent.com/PaulMayero/repomaker-fdroid/master/fdroid/repo",
        "https://raw.githubusercontent.com/inexcode/fdroid-repo/master",
        "https://raw.githubusercontent.com/liyuqihxc/fdroid_repo/master",
        "https://raw.githubusercontent.com/alienchristxv3/fgo_en_fdroid_repo/main/repo",
        "https://raw.githubusercontent.com/vaginessa/fdroid-1/gh-pages/repo",
        "https://raw.githubusercontent.com/vineelsai26/repo/main/fdroid/repo",
        "https://fdroid.ggtyler.dev/",
        "https://www.cromite.org/fdroid/repo",
        "http://anonero5wmhraxqsvzq2ncgptq6gq45qoto6fnkfwughfl4gbt44swad.onion/fdroid/repo",
        "https://app.futo.org/fdroid/repo",
        "https://f-droid.garykim.dev/fdroid/repo",
        "https://releases.threema.ch/fdroid/repo",
        "https://raw.githubusercontent.com/vaginessa/essential-repo/master/fdroid/repo",
        "https://raw.githubusercontent.com/ThatFinnDev/fullcodesfdroid/main/repo",
        "https://raw.githubusercontent.com/NovySoft/fdroid-repo/main/repo",
        "https://raw.githubusercontent.com/smartofficeschool/fdroid/master/fdroid/repo",
        "https://raw.githubusercontent.com/RaminAfhami993/fdroid/master/repo",
        "https://raw.githubusercontent.com/IgorKey/fdroid/master/fdroid/repo",
        "https://c10udburst.github.io/fdroid/repo/",
        "https://raw.githubusercontent.com/ohidurbappy/fdroid/main/fdroid/repo",
        "https://raw.githubusercontent.com/Ilingu/fdroid/main/repo",
        "https://raw.githubusercontent.com/25huizengek1/fdroid-repo/master/fdroid/repo",
        "http://fdroid.novy.software/archive",
        "https://raw.githubusercontent.com/zom/zom-android-nightly/master/fdroid/repo",
        "https://fdroid.pixelfed.net/fdroid/repo",
        "https://chen08209.github.io/FlClash-fdroid-repo/repo",
        "https://s3tupw1zard.github.io/fdroid/repo",
        "https://s3tupw1zard.github.io/fdroid/archive",
        "https://f5a.torus.icu/fdroid/repo",
        "https://raw.githubusercontent.com/Gabboxl/fdroids/master/fdroid/repo",
        "https://cheogram.com/fdroid/repo",
        "https://cheogram.com/fdroid/pre-release/fdroid/repo",
        "http://fdroid.coppernic.fr/common/fdroid/repo",
        "https://bazsalanszky.codeberg.page/fdroid/repo",
        "https://raw.githubusercontent.com/zaneschepke/fdroid/main/fdroid/repo",
        "http://warren-bank.github.io/fdroid/repo",
        "http://warren-bank.github.io/fdroid/archive",
        "https://fdroid.ironfoxoss.org/fdroid/repo",
        "https://f-droid.harrault.fr/fdroid/repo",
        "http://thunderbird.github.io/fdroid-thunderbird/repo",
        "https://updates.safing.io/fdroid/repo",
        "https://pcapdroid.org/fdroid/repo",
        "https://fdroid.bet/fdroid/repo",
        "https://repo.headuck.com/fdroid/repo",
        "https://fdroid.noql.net/fdroid/repo",
        "https://gh.artemchep.com/keyguard-repo-fdroid/repo",
        "https://selfprivacy.org/fdroid/repo",
        "https://fdroid.nearshare.shortdev.de/fdroid/repo",
        "https://reticulum.betweentheborders.com/fdroid/repo",
        "https://reticulum.betweentheborders.com/fdroid/archive",
        "https://pub.bma.ai/fdroid/repo",
        "https://maintainteam.github.io/fdroid-pages/fdroid/repo",
        "https://fdroid.foundationdevices.com/fdroid/repo",
        "https://f-droid.c3nav.de/fdroid/repo",
        "https://fdroid.fab-access.org/fdroid/repo",
        "https://files.podverse.fm/fdroid/repo",
        "https://files.podverse.fm/fdroid/archive",
        "https://raw.githubusercontent.com/nymtech/fdroid/main/fdroid/repo",
        "https://alpha.monerujo.io/fdroid/repo",
        "http://koutarou.uy/fdroid/repo",
        "https://raw.githubusercontent.com/FSlawiet/fdroid/main/fdroid/repo",
        "https://droid.lea.pet/",
        "https://litetex.github.io/fdroid-pages/fdroid/repo",
        "https://fdroid.escola.ch/fdroid/repo",
        "https://rosy-crow.app/fdroid/repo",
        "https://fdroid.i2pd.xyz/fdroid/repo",
        "https://fdroid.tagesschau.de/repo",
        "https://raw.githubusercontent.com/oxcl/fdroid-repo-tasker/refs/heads/main/direct-purchase/fdroid/repo",
        "https://raw.githubusercontent.com/jkulzer/fdroid-repo/refs/heads/main/repo",
        "https://raw.githubusercontent.com/malnvenshorn/fdroid-repository/refs/heads/master/fdroid/repo",
        "https://raw.githubusercontent.com/gopi487krishna/fdroidgk/refs/heads/main/fdroid/repo",
        "http://fdroid.armax.ru/fdroid/repo",
        "http://fdroid.armax.ru/fdroid/archive",
        "https://appstore.mifugo.go.tz/fdroid/repo",
        "https://pavluk.org/fdroid/repo",
        "https://getsignal.app/fdroid/repo",
        "https://kawaiiDango.github.io/pano-scrobbler/fdroid/repo",
        "https://eyedeekay.github.io/fdroid-dev/repo",
        "https://fdroid.ludikovsky.name/",
        "https://raw.githubusercontent.com/alt-droid/alt-droid-foss/fdroid/repo",
        "https://microg.org/fdroid/archive",
        "https://www.winomega.com/fdroid/repo",
        "http://lab.ene5ai.fr/repo",
        "https://app.rwth-aachen.de/fdroid/repo",
        "https://fdroid.libretro.com/archive",
        "https://haagch.frickel.club/files/fdroid/repo",
        "https://fdroid.monfluo.org/fdroid/repo",
        "https://soundcrowd.github.io/fdroid/repo",
        "https://fdroid.emersion.fr/goguma-nightly/repo",
        "https://raw.githubusercontent.com/provokateurin/nextcloud-neon-nightly/refs/heads/master/fdroid/repo",
        "https://raw.githubusercontent.com/cesaryuan/Cesar-FDroid-Repo/refs/heads/main/fdroid/repo",
        "https://raw.githubusercontent.com/cesaryuan/Cesar-FDroid-Repo/refs/heads/main/fdroid/archive",
        "https://raw.githubusercontent.com/cesaryuan/LSPosed-Modules-F-Droid/refs/heads/main/fdroid/repo",
        "https://raw.githubusercontent.com/cesaryuan/LSPosed-Modules-F-Droid/refs/heads/main/fdroid/archive",
        "https://raw.githubusercontent.com/Efreak/tachiyomi-extensions.old/refs/heads/master/fdroid/repo",
        "https://raw.githubusercontent.com/Efreak/tachiyomi-extensions.old/refs/heads/master/fdroid/archive",
        "https://raw.githubusercontent.com/efreak/seeker-fdroid/main/fdroid/repo",
        "https://raw.githubusercontent.com/Efreak/nontachi-fdroid/refs/heads/master/fdroid/repo",
        "https://raw.githubusercontent.com/Efreak/nontachi-fdroid/refs/heads/master/fdroid/archive",
        "https://raw.githubusercontent.com/KaplanBedwars/fdroid-repo/refs/heads/main/repo",
        "https://raw.githubusercontent.com/Tobi823/ffupdaterrepo/master/fdroid/repo",
        "http://gsantner.gitlab.io/fdroid/repo",
        "http://master.dl.sourceforge.net/project/fdroid-staging/repo",
        "http://derpy.ru/fdroid/repo",
        "https://apkrep.creativaxion.org/fdroid/repo",
        "https://fdroid.lds.online/repo",
        "https://fdroid.ipb.pt/repo/",
        "https://fdroid.ipb.pt/archive/",
        "https://fdroid.repo.chdft.net/fdroid/repo",
        "https://raw.githubusercontent.com/DARC-e-V/fdroid-repo/main/fdroid/repo",
        "https://packages.wcbing.top/fdroid/repo",
        "http://firefox-fdroid.endor.at/fdroid/repo",
        "http://repo.fasheng.info/fdroid/repo",
        "http://repo.fasheng.info/fdroid/archive",
        "https://f-droid.duniter.org/fdroid/repo",
        "https://files.drifty.win/repo",
        "https://exporl.med.kuleuven.be/fdroid/repo",
        "https://fdroid.shiftphones.com/fdroid/repo",
        "https://fdroid.shiftphones.com/fdroid/archive",
        "https://kotikone.xyz/fdroid/repo",
        "https://kotikone.xyz/fdroid/archive" */
  //  )
//}

object FdroidRepos {
    val OFFICIAL = FDroidRepo(
        name = "F-Droid",
        address = "https://f-droid.org/repo/",
        archiveUrl = "https://f-droid.org/archive/",
        publicKey = "43238d512c1e5eb2d6569f4a3afbf5523418b82e0a3ed15528acc99af5454b61"
    )

    // Per user request, focusing on a single JAR file source to fix the issue.
    val ALL = listOf(OFFICIAL)
}
