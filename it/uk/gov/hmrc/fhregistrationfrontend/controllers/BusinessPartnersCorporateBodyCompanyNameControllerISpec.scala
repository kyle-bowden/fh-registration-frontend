package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersCorporateBodyCompanyNameControllerISpec
  extends Specifications with TestConfiguration {

  val route: String = routes.BusinessPartnersCorporateBodyCompanyNameController.load().url.drop(6)
  val corpBodyTradingNameUrl: String = routes.BusinessPartnersCorporateBodyTradingNameController.load().url

  s"GET $route" when {

    "render the business partners corporate body company name page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include("What is the company name? - Business partners")
          page.getElementsByTag("h1").text must include("What is the company name?")
        }
      }
    }

  }

  s"POST $route" when {

    "the user submits with a company name" should {
      "redirect to the corporate body Trading Name page" when {
        "the user is authenticated" in {
          given.commonPrecondition

          val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "companyName" -> Seq("Shelby Limited")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(corpBodyTradingNameUrl)
          }
        }
      }
    }

    "User does not enter a company name" should {
      "return 400" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(
            "companyName" -> Seq("")
          ))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.title must include("What is the company name? - Business partners")
          page.getElementsByTag("h1").text() must include("What is the company name?")
          page.getElementById("companyName-error").text() must include("Enter a company name")
        }
      }
    }

    "User enters over 140 characters" should {
      "return 400" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(
            "companyName" -> Seq("ghfgdhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgf" +
              "hghfgdhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgfhghfg" +
              "dhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgfhs")
          ))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.title must include("What is the company name? - Business partners")
          page.getElementsByTag("h1").text() must include("What is the company name?")
          page.getElementById("companyName-error").text() must include("Company name must be 140 characters or less")
        }
      }
    }
  }
}