package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersPartnershipCompanyRegistrationNumberControllerISpec
  extends Specifications with TestConfiguration {

  val route: String = routes.BusinessPartnersPartnershipCompanyRegistrationNumberController.load().url.drop(6)
  val partnershipVatNumUrl: String = routes.BusinessPartnersPartnershipVatNumberController.load().url

  s"GET $route" should {
    "render the partnership-company-registration-number page" in {
      given
        .commonPrecondition

      val result = buildRequest(route)
        .addCookies(
          DefaultWSCookie("mdtp", authAndSessionCookie),
          DefaultWSCookie("businessType", "limited-liability-partnership")
        )
        .get()

      whenReady(result) { res =>
        res.status mustBe 200
        val page = Jsoup.parse(res.body)
        page.title() must include("What is the partnership’s company registration number?")
        page.getElementsByTag("h1").text() must include("What is Test Partnership’s company registration number?")
      }
    }
  }

  s"POST $route" when {
    "the companyRegistrationNumber is entered" should {
      "redirect to the Partnership VAT Registration Number page" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(
            DefaultWSCookie("mdtp", authAndSessionCookie),
            DefaultWSCookie("businessType", "limited-liability-partnership")
          )
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map("companyRegistrationNumber" -> Seq("01234567")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(partnershipVatNumUrl)
        }
      }

      "redirect to the Partnership VAT Registration Number page (letter formatted companyRegistrationNumber)" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(
            DefaultWSCookie("mdtp", authAndSessionCookie),
            DefaultWSCookie("businessType", "limited-liability-partnership")
          )
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map("companyRegistrationNumber" -> Seq("AB123456")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(partnershipVatNumUrl)
        }
      }
    }

    "companyRegistrationNumber is not supplied" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(
            DefaultWSCookie("mdtp", authAndSessionCookie),
            DefaultWSCookie("businessType", "limited-liability-partnership")
          )
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map("companyRegistrationNumber" -> Seq("")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter the company registration number")
        }
      }
    }

    "the companyRegistrationNumber field is invalid format" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(
            DefaultWSCookie("mdtp", authAndSessionCookie),
            DefaultWSCookie("businessType", "limited-liability-partnership")
          )
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map("companyRegistrationNumber" -> Seq("aaa")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Company registration number must be 8 numbers, or 2 letters followed by 6 numbers")
        }
      }
    }
  }
}
