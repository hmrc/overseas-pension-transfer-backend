import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.16"

lazy val microservice = Project("overseas-pension-transfer-backend", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalafmtOnCompile := true
  )
  .settings(inConfig(Test)(testSettings) *)
  .settings(CodeCoverageSettings.settings *)
  .settings(PlayKeys.playDefaultPort := 15601)

lazy val testSettings: Seq[Def.Setting[?]] = Seq(
  fork := true,
  Test / unmanagedResourceDirectories +=
    (baseDirectory.value / "test" / "uk" / "gov" / "hmrc" / "overseaspensiontransferbackend" / "resources")
)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.it)
