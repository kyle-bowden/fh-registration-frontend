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

class BusinessPartnersCorporateBodyUtrControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  val controller =
    new BusinessPartnersCorporateBodyUtrController(commonDependencies, views, mockActions, mockAppConfig)(mockMcc)

  "load" should {
    "Render the businessPartnersUtr page" when {
      "the new business partner pages are enabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.title() should include("What is the company’s Corporation Tax Unique Taxpayer Reference (UTR)?")
        reset(mockActions)
      }
    }

    "Render the Not found page" when {
      "the new business partner pages are disabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
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
      "return 303" when {
        "the form has no errors and UTR supplied" in {
          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              ("uniqueTaxpayerReference_yesNo", "true"),
              ("uniqueTaxpayerReference_value", "1234567890"))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(
            routes.BusinessPartnersCorporateBodyRegisteredAddressController.load().url)
          reset(mockActions)
        }
      }
    }

    "Render the Not found page" when {
      "the new business partner pages are disabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
        val request = FakeRequest()
          .withFormUrlEncodedBody(("chosenAddress", "1"))
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
