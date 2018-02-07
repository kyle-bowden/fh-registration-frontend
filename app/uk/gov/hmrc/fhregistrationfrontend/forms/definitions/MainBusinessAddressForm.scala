/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, of, optional}
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Constraints.oneOf
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.address
import uk.gov.hmrc.fhregistrationfrontend.forms.models.MainBusinessAddress
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.CustomFormatters.radioButton

object MainBusinessAddressForm {

  val mainBusinessAddressForm = Form(
    mapping(
      "timeAtCurrentAddress" → (nonEmptyText verifying oneOf(MainBusinessAddress.TimeAtCurrentAddressOptions)),
      "previousAddress" → optional(of(radioButton)),
      "mainPreviousAddressUK_previousAddress" → optional(address)
    )(MainBusinessAddress.apply)(MainBusinessAddress.unapply)
  )
}

