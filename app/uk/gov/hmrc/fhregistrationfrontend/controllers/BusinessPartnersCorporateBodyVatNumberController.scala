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

import com.google.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Results}
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.VatNumberForm.vatNumberForm
import uk.gov.hmrc.fhregistrationfrontend.forms.models.VatNumber
import uk.gov.hmrc.fhregistrationfrontend.views.Views

@Singleton
class BusinessPartnersCorporateBodyVatNumberController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {
  import actions._

  val form: Form[VatNumber] = vatNumberForm
  val corporateBody: String = "test corporateBody"
  val title: String = "corporateBody"
  val backAction: String = routes.BusinessPartnersPartnershipCompanyRegistrationNumberController.load().url

  def load(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      //ToDo read this data from the cache after being stored before the redirect
      val postAction = Call(
        method = "POST",
        url = uk.gov.hmrc.fhregistrationfrontend.controllers.routes.BusinessPartnersCorporateBodyVatNumberController
          .load()
          .url
      )
      Ok(view.business_partners_corporateBody_vat_number(form, corporateBody, title, postAction, backAction))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(): Action[AnyContent] = userAction { implicit request =>
    if (config.newBusinessPartnerPagesEnabled) {
      //ToDo read this data from the cache after being stored before the redirect
      vatNumberForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            val postAction = Call(
              method = "POST",
              url =
                uk.gov.hmrc.fhregistrationfrontend.controllers.routes.BusinessPartnersCorporateBodyVatNumberController
                  .next()
                  .url
            )
            BadRequest(view
              .business_partners_corporateBody_vat_number(formWithErrors, corporateBody, title, postAction, backAction))
          },
          vatNumber => {
            vatNumber.value match {
              case Some(vatNumber) => Redirect(routes.BusinessPartnerCorporateBodyRegisteredAddressController.load())
              case None            => Redirect(routes.BusinessPartnersCorporateBodyUtrController.load())
            }
          }
        )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }
}
