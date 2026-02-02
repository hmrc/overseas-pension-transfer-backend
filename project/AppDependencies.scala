import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.1.0"
  private val hmrcMongoVersion = "2.10.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % hmrcMongoVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"          %% "bootstrap-test-play-30"  % bootstrapVersion              % Test,
    "org.scalatest"        %% "scalatest"               % "3.2.19"                      % Test,
    "org.scalatestplus"    %% "scalacheck-1-17"         % "3.2.18.0"                    % Test,
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-test-play-30" % hmrcMongoVersion              % Test
  )

  val it: Seq[Nothing] = Seq.empty
}
