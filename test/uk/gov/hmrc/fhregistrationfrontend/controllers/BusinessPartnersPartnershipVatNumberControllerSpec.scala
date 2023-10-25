/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.fhregistrationfrontend.controllers

import com.codahale.metrics.SharedMetricRegistries
import org.jsoup.Jsoup
import org.mockito.Mockito.{reset, when}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import play.api.mvc.Cookie

class BusinessPartnersPartnershipVatNumberControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val partnershipUtrPage: String = routes.BusinessPartnersUtrController.load().url
  val partnershipRegisteredAddressPage: String = routes.BusinessPartnerPartnershipRegisteredAddressController.load().url

  val controller =
    new BusinessPartnersPartnershipVatNumberController(commonDependencies, views, mockActions, mockAppConfig)(mockMcc)

  "load" should {
    "Render the businessPartnersPartnershipVatNumber page" when {
      "the new business partner pages are enabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        when(mockAppConfig.getRandomBusinessType()).thenReturn("partnership")

        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.title() should include("Does the partner have a UK VAT registration number?")
        reset(mockActions)
      }
    }

    "Render the Not found page" when {
      "the new business partner pages are disabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
        when(mockAppConfig.getRandomBusinessType()).thenReturn("partnership")

        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        result.header.status shouldBe NOT_FOUND
        val page = Jsoup.parse(contentAsString(result))
        page.title() should include("Page not found")
        reset(mockActions)
      }
    }
  }

  "next" when {
    "the new business partner pages are enabled" should {
      "redirect to the Partnership SA UTR page" when {
        "Yes is selected and Vat Number supplied, and legal entity type is Partnership" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAppConfig.getRandomBusinessType()).thenReturn("partnership")
          val request = FakeRequest()
            .withCookies(Cookie("businessType", "partnership"))
            .withFormUrlEncodedBody(
              ("vatNumber_yesNo", "true"),
              ("vatNumber_value", "123456789")
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(partnershipUtrPage)
          reset(mockActions)
        }

        "No is selected, and legal entity type is Partnership" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAppConfig.getRandomBusinessType()).thenReturn("partnership")
          val request = FakeRequest()
            .withCookies(Cookie("businessType", "partnership"))
            .withFormUrlEncodedBody(
              ("vatNumber_yesNo", "false")
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(partnershipUtrPage)
          reset(mockActions)
        }

        "No is selected, and legal entity type is Limited Liability Partnership" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAppConfig.getRandomBusinessType()).thenReturn("limited-liability-partnership")
          val request = FakeRequest()
            .withCookies(Cookie("businessType", "limited-liability-partnership"))
            .withFormUrlEncodedBody("vatNumber_yesNo" -> "false")
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(partnershipUtrPage)
          reset(mockActions)
        }
      }

      "redirect to the Partnership Registered Office Address page" when {
        "Yes is selected and Vat Number supplied, and legal entity type is Limited Liability Partnership" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAppConfig.getRandomBusinessType()).thenReturn("limited-liability-partnership")

          val request = FakeRequest()
            .withCookies(Cookie("businessType", "limited-liability-partnership"))
            .withFormUrlEncodedBody(
              ("vatNumber_yesNo", "true"),
              ("vatNumber_value", "123456789")
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(partnershipRegisteredAddressPage)
          reset(mockActions)
        }
      }

      "Return the correct error" when {
        "the user selects yes but doesn't enter a VAT number" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAppConfig.getRandomBusinessType()).thenReturn("limited-liability-partnership")
          val request = FakeRequest()
            .withCookies(Cookie("businessType", "limited-liability-partnership"))
            .withFormUrlEncodedBody(
              ("vatNumber_yesNo", "true"),
              ("vatNumber_value", "")
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe BAD_REQUEST
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("Does the partner have a UK VAT registration number?")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() should include(
            "Enter the VAT registration number")
          reset(mockActions)
        }
      }
    }

    "Render the Not found page" when {
      "the new business partner pages are disabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
        val request = FakeRequest()
          .withFormUrlEncodedBody(("vatNumber_yesNo", "true"))
          .withMethod("POST")
        val result = await(csrfAddToken(controller.next())(request))

        status(result) shouldBe NOT_FOUND
        val page = Jsoup.parse(contentAsString(result))
        page.title() should include("Page not found")
        reset(mockActions)
      }
    }
  }
}
