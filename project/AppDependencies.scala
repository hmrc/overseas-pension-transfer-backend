import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.13.0"
  private val hmrcMongoVersion = "2.6.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % hmrcMongoVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"          %% "bootstrap-test-play-30"  % bootstrapVersion              % Test,
    "org.scalatest"        %% "scalatest"               % "3.2.19"                      % Test,
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-test-play-30" % hmrcMongoVersion              % Test,
    "org.mockito"          %% "mockito-scala-scalatest" % "1.17.37"                     % Test,
  )

  val it: Seq[Nothing] = Seq.empty
}
