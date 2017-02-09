import ReleaseTransformations._

scalaVersion := "2.11.7"

crossScalaVersions := Seq("2.11.7", "2.10.5")

organization in ThisBuild := "com.trueaccord.lenses"

scalacOptions in ThisBuild ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v <= 11 => List("-target:jvm-1.6")
    case _ => Nil
  }
}

releaseCrossBuild := true

releasePublishArtifactsAction := PgpKeys.publishSigned.value

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _), enableCrossBuild = true),
  setNextVersion,
  commitNextVersion,
  pushChanges,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _), enableCrossBuild = true)
)

lazy val root = project.in(file("."))
  .aggregate(lensesJS, lensesJVM)
  .settings(
    publish := {},
    publishLocal := {},
    publishArtifact := false
  )

lazy val lenses = crossProject.in(file("."))
  .settings(
    name := "lenses",
    publishMavenStyle := true,
    credentials += {
      def file = "credentials-" + (if (isSnapshot.value) "snapshots" else "internal")

      Credentials(Path.userHome / ".m2" / file)
    },
    publishTo := {
      def path = "/repository/" + (if (isSnapshot.value) "snapshots" else "internal")

      Some("CodeMettle Maven" at s"http://maven.codemettle.com$path")
    }
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % "1.13.4" % "test",
      "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
    )
  )
  .jsSettings(
    scalacOptions += {
      val a = (baseDirectory in LocalRootProject).value.toURI.toString
      val g = "https://raw.githubusercontent.com/trueaccord/Lenses/" + sys.process.Process("git rev-parse HEAD").lines_!.head
      s"-P:scalajs:mapSourceURI:$a->$g/"
    }
  )

lazy val lensesJVM = lenses.jvm
lazy val lensesJS = lenses.js

