package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnerCorporateBodyRegisteredAddressControllerISpec
  extends Specifications with TestConfiguration {

  val route: String = routes.BusinessPartnersCorporateBodyRegisteredAddressController.load().url.drop(6)
  val chooseAddressUrl: String = routes.BusinessPartnersChooseAddressController.load().url
  val corpBodyConfirmRegAddressUrl: String = routes.BusinessPartnersCorporateBodyConfirmRegisteredAddressController.load().url
  val cannotFindAddressUrl: String = routes.BusinessPartnersCannotFindAddressController.load().url

  s"GET $route" when {

    "the new business partners flow is enabled" should {

      "render the business partner corporate body registered address page" when {
        "the user is authenticated" in {
          given.commonPrecondition

          val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title() must include("What is the company’s registered office address?")
              page.getElementsByTag("h1").text() must include("What is Test Corporate Body’s registered office address?")
            }
          }
        }
      }
    }

  s"POST $route" when {
    "the form has no errors and multiple addresses are found" should {
      "return 303" in {
        given.commonPreconditionWithMultipleAddressLookup(true)

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
          "Csrf-Token" -> "nocheck").withFollowRedirects(false)
          .post(Map("partnerAddressLine" -> Seq("Drury Lane"),
            "partnerPostcode" -> Seq("AB1 2YZ")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(chooseAddressUrl)
        }
      }
    }

    "the form has no errors and a matching address is found" should {
      "return 303" in {
        given.commonPreconditionWithSingleAddressLookup(true)

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
          "Csrf-Token" -> "nocheck").withFollowRedirects(false)
          .post(Map("partnerAddressLine" -> Seq("1 Romford Road"),
            "partnerPostcode" -> Seq("TF1 4ER")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(corpBodyConfirmRegAddressUrl)
        }
      }
    }

    "postcode supplied but address line not populated" should {
      "return 303" in {
        given.commonPreconditionWithMultipleAddressLookup(true)

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck").withFollowRedirects(false)
          .post(Map("partnerAddressLine" -> Seq.empty,
            "partnerPostcode" -> Seq("AB1 2YZ")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(chooseAddressUrl)
        }
      }
    }

    "address cannot be found for postcode" should {
      "return 303" in {
        given.commonPreconditionWithEmptyAddressLookup(true)

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck").withFollowRedirects(false)
          .post(Map("partnerAddressLine" -> Seq.empty,
            "partnerPostcode" -> Seq("AB1 2YX")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(cannotFindAddressUrl)
        }
      }
    }

    "address lookup service request is unsuccessful" should {
      "return 400" in {
        given.commonPreconditionWithEmptyAddressLookup(false)

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck").withFollowRedirects(false)
          .post(Map("partnerAddressLine" -> Seq.empty,
            "partnerPostcode" -> Seq("AB1 2YX")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Sorry, there was problem performing this search, please try again and if the problem persists then enter the address manually")
        }
      }
    }

    "postcode not populated" should {
      "return 400" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
          "Csrf-Token" -> "nocheck")
          .post(Map("partnerAddressLine" -> Seq("1"),
            "partnerPostcode" -> Seq.empty))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-error-summary").text() must include("Enter the postcode of the address")
        }
      }
    }

    "postcode invalid format" should {
      "return 400" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
          "Csrf-Token" -> "nocheck")
          .post(Map("partnerAddressLine" -> Seq("1"),
            "partnerPostcode" -> Seq("A")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter a valid postcode")
        }
      }
    }

    "address line contains invalid characters" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
          "Csrf-Token" -> "nocheck")
          .post(Map("partnerAddressLine" -> Seq("The lane;"),
            "partnerPostcode" -> Seq("AB1 2YZ")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-error-summary").text() must include("only include letters a to z, numbers, apostrophes, commas, dashes, exclamation marks, forward slashes, full stops, hyphens, quotation marks, round brackets and spaces")
        }
      }
    }

    "address line contains more than 35 characters" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
          "Csrf-Token" -> "nocheck")
          .post(Map("partnerAddressLine" -> Seq("qwertyuiopasdfghjklzxcvbnmqwkydvkdsgvisudgfkjsdvkjsdcjkdh"),
            "partnerPostcode" -> Seq("AB1 2YZ")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-error-summary").text() must include("Address lines must not be longer than 35 characters")
        }
      }
    }
  }
}