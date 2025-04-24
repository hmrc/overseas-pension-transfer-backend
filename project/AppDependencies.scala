import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.11.0"

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapVersion
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion              % Test,
    "org.scalatest"     %% "scalatest"               % "3.2.19"                      % Test,
    "org.mockito"       %% "mockito-scala-scalatest" % "1.17.37"                     % Test,
  )

  val it = Seq.empty
}
