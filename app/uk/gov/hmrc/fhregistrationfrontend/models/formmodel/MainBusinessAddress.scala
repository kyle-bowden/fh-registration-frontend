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

package uk.gov.hmrc.fhregistrationfrontend.models.formmodel

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.fhregistrationfrontend.models.des.Address

case class MainBusinessAddress(period: String, hasOtherAddress: Option[Boolean], previousAddress: Option[Address])

object MainBusinessAddress {

  implicit val format: OFormat[MainBusinessAddress] = Json.format[MainBusinessAddress]

  val PERIOD = "period"
  val HAS_OTHER_ADDRESS = "hasOtherAddress"
  val PREVIOUS_ADDRESS = "previousAddress"

  def mainBusinessAddressForm = Form(
    mapping(
      PERIOD -> nonEmptyText,
      HAS_OTHER_ADDRESS -> optional(of(CustomFormatters.requiredBooleanFormatter)),
      PREVIOUS_ADDRESS -> optional(Address.addressMapping)
    )(MainBusinessAddress.apply)(MainBusinessAddress.unapply)
  )

  def hideField(mainBusinessAddress: Form[MainBusinessAddress]): String = {
    if (mainBusinessAddress("period").hasErrors) ""
    else {
      mainBusinessAddress.value match {
        case Some(v) => if (v.period == "Less than 3 years") "" else "hidden"
        case _ => "hidden"
      }
    }
  }

  def hideAddressField(mainBusinessAddress: Form[MainBusinessAddress]): String = {
    if (mainBusinessAddress("hasOtherAddress").hasErrors) ""
    else {
      mainBusinessAddress.value match {
        case Some(v) => if (v.hasOtherAddress == "true") "" else "hidden"
        case _ => "hidden"
      }
    }
  }

}



