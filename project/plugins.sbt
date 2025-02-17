resolvers += Resolver.url("scoverage-bintray", url("https://dl.bintray.com/sksamuel/sbt-plugins/"))(Resolver.ivyStylePatterns)
resolvers += "HMRC Releases" at "https://artefacts.tax.service.gov.uk/artifactory/hmrc-releases/"
resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
resolvers += Resolver.jcenterRepo

addSbtPlugin("uk.gov.hmrc"         %   "sbt-auto-build"        % "3.14.0")
addSbtPlugin("uk.gov.hmrc"         %   "sbt-distributables"    % "2.2.0")
addSbtPlugin("com.typesafe.play"   %   "sbt-plugin"            % "2.8.18")
addSbtPlugin("org.scoverage"       %   "sbt-scoverage"         % "1.9.3")
addSbtPlugin("org.scalastyle"     %%   "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("com.lucidchart"      %   "sbt-scalafmt"          % "1.16")
addSbtPlugin("org.irundaia.sbt"    %   "sbt-sassify"           % "1.4.13")
