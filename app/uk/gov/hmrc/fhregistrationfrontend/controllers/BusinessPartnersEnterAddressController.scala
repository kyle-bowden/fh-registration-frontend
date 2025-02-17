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

import play.api.data.Form
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessPartnersEnterAddressForm.chooseAddressForm
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnersEnterAddress
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import javax.inject.Inject

class BusinessPartnersEnterAddressController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {

  val partnerName: String = "Test User"
  val journeyType: String = "enterAddress"
  val backUrl: String = routes.BusinessPartnerAddressController.load().url
  val postAction: Call = routes.BusinessPartnersEnterAddressController.next()

  import actions._
  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      // Todo get this from cache later
      Ok(view.business_partners_enter_address(chooseAddressForm, postAction, partnerName, journeyType, backUrl))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      // Todo get this from cache later
      chooseAddressForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            BadRequest(
              view.business_partners_enter_address(formWithErrors, postAction, partnerName, journeyType, backUrl))
          },
          bpAddress => {
            Redirect(routes.BusinessPartnersCheckYourAnswersController.load("individual"))
          }
        )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }
}
