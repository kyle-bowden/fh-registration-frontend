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

package uk.gov.hmrc.fhregistrationfrontend.services

import com.google.inject.ImplementedBy
import org.joda.time.LocalDate
import play.api.libs.json
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.fhregistrationfrontend.cache.ShortLivedCache
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.ShortLivedCache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Save4LaterKeys {
  val businessRegistrationDetailsKey = "businessRegistrationDetails"
  val businessTypeKey = "businessType"
  val userLestTimeUsedKey = "userLestTimeUsed"
}

@ImplementedBy(classOf[Save4LaterServiceImpl])
trait Save4LaterService {

  import Save4LaterKeys._

  implicit val dateReads: Reads[LocalDate] = Reads.jodaLocalDateReads("yyyy-MM-dd")
  implicit val dateWrites: Writes[LocalDate] = Writes.jodaLocalDateWrites("yyyy-MM-dd")

  val shortLivedCache: ShortLivedCache

  def saveBusinessType(userId: String, businessType: String)(implicit hc: HeaderCarrier) = {
    saveData4Later(userId, businessTypeKey, businessType)
  }

  def saveBusinessRegistrationDetails(userId: String, brd: BusinessRegistrationDetails)(implicit hc: HeaderCarrier) = {
    saveData4Later(userId, businessRegistrationDetailsKey, brd)
  }

  def fetchBusinessRegistrationDetails(userId: String)(implicit hc: HeaderCarrier) = {
    fetchData4Later[BusinessRegistrationDetails](userId, businessRegistrationDetailsKey)
  }

  def saveTime(userId: String, userLestTimeUsed: LocalDate)(implicit hc: HeaderCarrier) = {
    shortLivedCache.cache(userId, userLestTimeUsedKey, userLestTimeUsed) map {
      data ⇒ data.getEntry(userId)
    }
  }

  def fetchTime(userId: String)(implicit hc: HeaderCarrier): Future[Option[LocalDate]] = {
    fetchData4Later[LocalDate](userId, userLestTimeUsedKey)
  }

  @inline def saveData4Later[T](id: String, formId: String, data: T)(implicit hc: HeaderCarrier, formats: json.Format[T]): Future[Option[T]] = {
    val userLestTimeUsed: LocalDate = new LocalDate()
    saveTime(id, userLestTimeUsed).flatMap { _ ⇒
      shortLivedCache.cache(id, formId, data) map {
        data ⇒ data.getEntry[T](formId)
      }
    }
  }

  @inline def fetchData4Later[T](utr: String, formId: String)(implicit hc: HeaderCarrier, formats: json.Format[T]): Future[Option[T]] =
    shortLivedCache.fetchAndGetEntry[T](utr, formId)

}

class Save4LaterServiceImpl extends Save4LaterService {
  override val shortLivedCache = ShortLivedCache
}
