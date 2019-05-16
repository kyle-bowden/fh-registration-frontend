/*
 * Copyright 2019 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.i18n.MessagesProvider
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, AnyContent, BodyParser, MessagesControllerComponents}
import uk.gov.hmrc.fhregistrationfrontend.config.{AppConfig, FrontendAppConfig}
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.controllers.AdminRequest.requestForm
import uk.gov.hmrc.fhregistrationfrontend.controllers.EnrolmentForm.{allocateEnrolmentForm, deleteEnrolmentForm}
import uk.gov.hmrc.fhregistrationfrontend.views.html._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdminPageController @Inject()(
  frontendAppConfig: FrontendAppConfig,
  appConfig: AppConfig,
  fhddsConnector: FhddsConnector,
  cc: MessagesControllerComponents
)(implicit ec: ExecutionContext, messagesProvider: MessagesProvider) extends FrontendController(cc) {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  lazy val credentials = Credentials(frontendAppConfig.username, frontendAppConfig.password)
  val bodyParser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser
  val authAction = AuthenticationController(credentials, bodyParser)

  def showAdminPage: Action[AnyContent] = authAction { implicit request =>

    Ok(temp_admin_page(request, frontendAppConfig, messagesProvider))
  }

  def getSubmissions: Action[AnyContent] = authAction.async { implicit request =>

    fhddsConnector.getAllSubmission().map {
      case submissions if submissions.nonEmpty => Ok(show_all_submissions(submissions)(request,frontendAppConfig, messagesProvider))
      case submissions if submissions.isEmpty => Ok("No Submissions found")
    }
  }

  def loadDeletePage(formBundleId:String)= authAction.async { implicit request =>

    fhddsConnector.getSubMission(formBundleId).map {
      case submission => Ok(show_submission(submission)(request, frontendAppConfig, messagesProvider))
      case _ => Ok(s"No Submission found for $formBundleId")
    }
  }

  def deleteSubmission(formBundleId:String): Action[AnyContent] = authAction.async { implicit request =>
    fhddsConnector.deleteSubmission(formBundleId).map(_ => Ok(s"Submission data for $formBundleId has been deleted "))
      .recover{case _ => Ok(s"Submission with $formBundleId not found")}

  }

  def loadUserIdPage = authAction { implicit request =>

    Ok(admin_get_groupID(requestForm)(request, frontendAppConfig, messagesProvider))
  }

  def sendAdminRequest() = authAction.async { implicit request =>
    requestForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(admin_get_groupID(formWithErrors)(request, frontendAppConfig, messagesProvider)))
      },

     formData =>
       fhddsConnector.addEnrolment(formData.userId, formData.groupId, formData.registrationNumber).map(result => Ok(result.body))
    )
  }

  def loadAllocateEnrolment = authAction {
    implicit request =>
      Ok(allocate_enrolment(allocateEnrolmentForm)(request, frontendAppConfig, messagesProvider))
  }

  def allocateEnrolment = authAction.async {
    implicit request  =>
      allocateEnrolmentForm.bindFromRequest.fold (
        formWithErrors => {
          Future.successful(BadRequest(allocate_enrolment(formWithErrors)(request, frontendAppConfig, messagesProvider)))
        },

        formData => {
          fhddsConnector.allocateEnrolment(formData.userId, formData.registrationNumber).map(result => Ok(result.body))
        }
      )
  }

  def loadDeleteEnrolment = authAction {
    implicit request =>
      Ok(delete_enrolment(deleteEnrolmentForm)(request, frontendAppConfig, messagesProvider))
  }

  def deleteEnrolment = authAction.async {
    implicit request =>
      deleteEnrolmentForm.bindFromRequest.fold (
        formWithErrors => {
          Future.successful(BadRequest(delete_enrolment(formWithErrors)(request, frontendAppConfig, messagesProvider)))

        },

        formData => {
          fhddsConnector.deleteEnrolment(formData.userId, formData.registrationNumber).map(result => Ok(result.body))
        }
      )
  }

  def checkStatus(regNo: String) = authAction.async {
    implicit request  =>
      fhddsConnector
        .getStatus(regNo)(hc).map(result => Ok(result.toString))
  }

  def getUserInfo(userId: String) = authAction.async { implicit request =>
    fhddsConnector.getUserInfo(userId).map {
      response => Ok(response.json)
    }
  }

  def getGroupInfo(groupId: String) = authAction.async { implicit request =>
    fhddsConnector.getGroupInfo(groupId).map {
      response => Ok(response.json)
    }
  }

  def es2(userId: String) = authAction.async { implicit request =>
    fhddsConnector.es2Info(userId).map {
      response => Ok(response.json)
    }
  }

  def es3(groupId: String) = authAction.async { implicit request =>
    fhddsConnector.es3Info(groupId).map {
      response => Ok(response.json)
    }
  }
}

case class AdminRequest(userId:String, groupId:String, registrationNumber: String)

object AdminRequest {
  val format: Format[AdminRequest] = Json.format[AdminRequest]

  val requestForm: Form[AdminRequest] = Form(mapping(
    "userId" -> nonEmptyText,
    "groupId" -> nonEmptyText,
    "registrationNumber" -> nonEmptyText
  )(AdminRequest.apply)(AdminRequest.unapply))
}

case class EnrolmentForm(userId:String, registrationNumber: String)

object EnrolmentForm {

  val format: Format[EnrolmentForm] = Json.format[EnrolmentForm]

  val allocateEnrolmentForm: Form[EnrolmentForm] = Form(mapping(
    "userId" -> nonEmptyText,
    "registrationNumber" -> nonEmptyText
  )(EnrolmentForm.apply)(EnrolmentForm.unapply))

  val deleteEnrolmentForm: Form[EnrolmentForm] = allocateEnrolmentForm
}



