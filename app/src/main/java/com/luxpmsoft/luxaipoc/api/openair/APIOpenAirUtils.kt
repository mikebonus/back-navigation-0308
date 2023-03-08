package com.luxpmsoft.luxaipoc.api.openair

import android.util.Log
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.APIService
import com.luxpmsoft.luxaipoc.api.body.APIBodyUtils
import com.luxpmsoft.luxaipoc.model.BaseResponse
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.comment.CommentRequest
import com.luxpmsoft.luxaipoc.model.comment.CommentResponse
import com.luxpmsoft.luxaipoc.model.home.OrganizationDashboardResponse
import com.luxpmsoft.luxaipoc.model.home.SubscriptionResponse
import com.luxpmsoft.luxaipoc.model.login.LoginRequest
import com.luxpmsoft.luxaipoc.model.login.LoginResponse
import com.luxpmsoft.luxaipoc.model.login.LoginSNSRequest
import com.luxpmsoft.luxaipoc.model.notification.NotificationListRequest
import com.luxpmsoft.luxaipoc.model.notification.NotificationResponse
import com.luxpmsoft.luxaipoc.model.organization.*
import com.luxpmsoft.luxaipoc.model.project_management.CreateBoardResponse
import com.luxpmsoft.luxaipoc.model.project_management.CreateProjectResponse
import com.luxpmsoft.luxaipoc.model.project_management.ProjectManagementResponse
import com.luxpmsoft.luxaipoc.model.project_management.request.BoardRequest
import com.luxpmsoft.luxaipoc.model.project_management.request.ProjectRequest
import com.luxpmsoft.luxaipoc.model.recentmodel.*
import com.luxpmsoft.luxaipoc.model.recentmodel.request.AddTagRequest
import com.luxpmsoft.luxaipoc.model.recentmodel.request.CadFileRequest
import com.luxpmsoft.luxaipoc.model.recentmodel.request.SaveCadFileRequest
import com.luxpmsoft.luxaipoc.model.recentmodel.request.TagRequest
import com.luxpmsoft.luxaipoc.model.reconstruction.RenameModelRequest
import com.luxpmsoft.luxaipoc.model.reconstruction.RenameModelResponse
import com.luxpmsoft.luxaipoc.model.register.*
import com.luxpmsoft.luxaipoc.model.repositories.*
import com.luxpmsoft.luxaipoc.model.repositories.request.AddUserRequest
import com.luxpmsoft.luxaipoc.model.repositories.request.PhotoRepositoryRequest
import com.luxpmsoft.luxaipoc.model.repositories.request.RepositoryRequest
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionRequest
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionTypeDetailResponse
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionTypeResponse
import com.luxpmsoft.luxaipoc.model.user.*
import com.luxpmsoft.luxaipoc.model.workspaces.CreateWorkspaceResponse
import com.luxpmsoft.luxaipoc.model.workspaces.EditWorkspaceResponse
import com.luxpmsoft.luxaipoc.model.workspaces.WorkspaceResponse
import com.luxpmsoft.luxaipoc.model.workspaces.request.EditWorkspaceRequest
import com.luxpmsoft.luxaipoc.model.workspaces.request.WorkspaceRequest
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class APIOpenAirUtils {
    companion object {
        const val TAG = "APIARUtils"

        fun getData(): APIService {
            return APIOpenAirManager.client.create(APIService::class.java)
        }

        //call api login
        fun login(loginRequest: LoginRequest, delegate: APIInterface.onDelegate) {
            val call: Call<LoginResponse> = getData().login(loginRequest)
            call.enqueue(object : Callback<LoginResponse> {
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //call api login SNS
        fun loginSNS(loginRequest: LoginSNSRequest, delegate: APIInterface.onDelegate) {
            val call: Call<LoginResponse> = getData().loginSNS(loginRequest)
            call.enqueue(object : Callback<LoginResponse> {
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //get Subscription
        fun getSubscriptionUser(accessToken: String, delegate: APIInterface.onDelegate) {
            val call: Call<SubscriptionResponse> = getData().getSubscriptionUser(accessToken)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<SubscriptionResponse> {
                override fun onFailure(call: Call<SubscriptionResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<SubscriptionResponse>,
                    response: Response<SubscriptionResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //get dashboard
        fun getDashboard(organizationId: String?, accessToken: String, delegate: APIInterface.onDelegate) {
            val call: Call<OrganizationDashboardResponse> = getData().getDashboard(organizationId, accessToken)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<OrganizationDashboardResponse> {
                override fun onFailure(call: Call<OrganizationDashboardResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<OrganizationDashboardResponse>,
                    response: Response<OrganizationDashboardResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //call addSubscription
        fun addSubscription(accessToken: String, subscriptionRequest: SubscriptionRequest, delegate: APIInterface.onDelegate) {
            val call: Call<SubscriptionResponse> = getData().addSubscriptionUser(accessToken, subscriptionRequest)
            call.enqueue(object : Callback<SubscriptionResponse> {
                override fun onFailure(call: Call<SubscriptionResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<SubscriptionResponse>,
                    response: Response<SubscriptionResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //get Subscription
        fun getReconstruction(accessToken: String, pageIndex: Int?,pageSize: Int?,
                              sort: String?, sortType: String?, filter: String, organizationId: String?, search: String?, delegate: APIInterface.onDelegate) {
            val call: Call<ReconstructionResponse> = getData().getReconstruction(accessToken, pageIndex, pageSize, filter,sort, sortType, search, organizationId)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<ReconstructionResponse> {
                override fun onFailure(call: Call<ReconstructionResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<ReconstructionResponse>,
                    response: Response<ReconstructionResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        fun sceneReconstruction(accessToken: String, requestBody: RequestBody, delegate: APIInterface.onDelegate) {
            val call: Call<ResponseBody> = getData().sceneReconstruction(accessToken, requestBody)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //get Cad file
        fun getCadFile(accessToken: String, pageIndex: Int?,pageSize: Int?,
                       boardId: String?, search: String?,delegate: APIInterface.onDelegate) {
            val call: Call<CadFileResponse> = getData().getCadFile(accessToken, boardId, pageIndex, pageSize, search)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<CadFileResponse> {
                override fun onFailure(call: Call<CadFileResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CadFileResponse>,
                    response: Response<CadFileResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //get Cad file detail
        fun getCadFileDetail(accessToken: String, id: String, delegate: APIInterface.onDelegate) {
            val call: Call<CadFileDetailResponse> = getData().getCadFileDetail(accessToken, id)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<CadFileDetailResponse> {
                override fun onFailure(call: Call<CadFileDetailResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CadFileDetailResponse>,
                    response: Response<CadFileDetailResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //call delete file
        fun deleteFile(accessToken: String?, id: String?, delegate: APIInterface.onDelegate) {
            val call: Call<CreateBoardResponse> = getData().deleteFile(accessToken, id)
            call.enqueue(object : Callback<CreateBoardResponse> {
                override fun onFailure(call: Call<CreateBoardResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CreateBoardResponse>,
                    response: Response<CreateBoardResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //get add file
        fun addFile(accessToken: String, organizationId: String?, cadFileRequest: CadFileRequest, delegate: APIInterface.onDelegate) {
            val call: Call<CadFileResponse> = getData().addCadFile(accessToken, organizationId, cadFileRequest)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<CadFileResponse> {
                override fun onFailure(call: Call<CadFileResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CadFileResponse>,
                    response: Response<CadFileResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //get save cad file
        fun saveCadFile(accessToken: String, cadFileId: String?, cadFileRequest: SaveCadFileRequest, delegate: APIInterface.onDelegate) {
            val call: Call<CadFileResponse> = getData().saveCadFile(accessToken, cadFileId, cadFileRequest)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<CadFileResponse> {
                override fun onFailure(call: Call<CadFileResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CadFileResponse>,
                    response: Response<CadFileResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //view model
        fun getReconstructionHTML(accessToken: String, id: String?, delegate: APIInterface.onDelegate) {
            val call: Call<ResponseBody> = getData().getReconstructionHTML(accessToken, id)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //view model
        fun getModel(accessToken: String, reconstructionID: String?, delegate: APIInterface.onDelegate) {
            val call: Call<ResponseBody> = getData().getModel(accessToken, reconstructionID)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //view model
        fun renameModel(accessToken: String, body: RenameModelRequest, delegate: APIInterface.onDelegate) {
            val call: Call<RenameModelResponse> = getData().renameModel(accessToken, body)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<RenameModelResponse> {
                override fun onFailure(call: Call<RenameModelResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<RenameModelResponse>,
                    response: Response<RenameModelResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //delete model
        fun deleteModel(accessToken: String, reconstructionID: String, delegate: APIInterface.onDelegate) {
            val call: Call<BaseResponse> = getData().deleteModel(accessToken, reconstructionID)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<BaseResponse> {
                override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<BaseResponse>,
                    response: Response<BaseResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //get user detail
        fun getUserDetail(accessToken: String, delegate: APIInterface.onDelegate) {
            val call: Call<UserDetailResponse> = getData().getUserDetail(accessToken)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<UserDetailResponse> {
                override fun onFailure(call: Call<UserDetailResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<UserDetailResponse>,
                    response: Response<UserDetailResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //get me
        fun getMe(accessToken: String, delegate: APIInterface.onDelegate) {
            val call: Call<MeResponse> = getData().getMe(accessToken)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<MeResponse> {
                override fun onFailure(call: Call<MeResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<MeResponse>,
                    response: Response<MeResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //uploadAvatar
        fun updateUser(accessToken: String, user: UpdateUserRequest, delegate: APIInterface.onDelegate) {
            val call: Call<UserImageResponse> = getData().updateUser(accessToken, user)
            call.enqueue(object : Callback<UserImageResponse> {
                override fun onFailure(call: Call<UserImageResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<UserImageResponse>,
                    response: Response<UserImageResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //uploadAvatar
        fun uploadAvatar(accessToken: String, requestBody: RequestBody, delegate: APIInterface.onDelegate) {
            val call: Call<UserImageResponse> = getData().uploadAvatar(accessToken, requestBody)
            call.enqueue(object : Callback<UserImageResponse> {
                override fun onFailure(call: Call<UserImageResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<UserImageResponse>,
                    response: Response<UserImageResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //api add comment
        fun addComment(accessToken: String, organizationId: String?, commentRequest: CommentRequest, delegate: APIInterface.onDelegate) {
            val call: Call<CommentResponse> = getData().addComment(accessToken, organizationId, commentRequest)
            call.enqueue(object : Callback<CommentResponse> {
                override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CommentResponse>,
                    response: Response<CommentResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //api get comment
        fun getComment(accessToken: String, commentId: String?, delegate: APIInterface.onDelegate) {
            val call: Call<CommentResponse> = getData().getComment(accessToken, commentId)
            call.enqueue(object : Callback<CommentResponse> {
                override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CommentResponse>,
                    response: Response<CommentResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }


        //call sendOTP
        fun sendOTP(sendOTPRequest: SendOTPRequest, delegate: APIInterface.onDelegate) {
            val call: Call<SendOTPResponse> = getData().sendOTP(sendOTPRequest)
            call.enqueue(object : Callback<SendOTPResponse> {
                override fun onFailure(call: Call<SendOTPResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<SendOTPResponse>,
                    response: Response<SendOTPResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //call register
        fun register(register: RegisterRequest, delegate: APIInterface.onDelegate) {
            val call: Call<RegisterResponse> = getData().register(register)
            call.enqueue(object : Callback<RegisterResponse> {
                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<RegisterResponse>,
                    response: Response<RegisterResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //call emailVerification
        fun emailVerification(emailVerificationRequest: EmailVerificationRequest, delegate: APIInterface.onDelegate) {
            val call: Call<EmailVerificationResponse> = getData().emailVerification(emailVerificationRequest)
            call.enqueue(object : Callback<EmailVerificationResponse> {
                override fun onFailure(call: Call<EmailVerificationResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<EmailVerificationResponse>,
                    response: Response<EmailVerificationResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //call resetSendOTP
        fun resetSendOTP(sendOTPRequest: SendOTPRequest, delegate: APIInterface.onDelegate) {
            val call: Call<SendOTPResponse> = getData().resetSendOTP(sendOTPRequest)
            call.enqueue(object : Callback<SendOTPResponse> {
                override fun onFailure(call: Call<SendOTPResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<SendOTPResponse>,
                    response: Response<SendOTPResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //call verifyOTP
        fun verifyOTP(verifyOTPRequest: VerifyOTPRequest, delegate: APIInterface.onDelegate) {
            val call: Call<VerifyOTPResponse> = getData().verifyOTP(verifyOTPRequest)
            call.enqueue(object : Callback<VerifyOTPResponse> {
                override fun onFailure(call: Call<VerifyOTPResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<VerifyOTPResponse>,
                    response: Response<VerifyOTPResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //change Password
        fun changePassword(accessToken: String, changePassword: ChangePasswordRequest, delegate: APIInterface.onDelegate) {
            val call: Call<ResetPasswordResponse> = getData().changePassword(accessToken, changePassword)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<ResetPasswordResponse> {
                override fun onFailure(call: Call<ResetPasswordResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<ResetPasswordResponse>,
                    response: Response<ResetPasswordResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //forget Password
        fun forgetPassword(accessToken: String, forgetPasswordRequest: ForgetPasswordRequest, delegate: APIInterface.onDelegate) {
            val call: Call<ForgetPasswordResponse> = getData().forgetPassword(accessToken, forgetPasswordRequest)
            call.enqueue( object : Callback<ForgetPasswordResponse>{
                override fun onResponse(
                    call: Call<ForgetPasswordResponse>,
                    response: Response<ForgetPasswordResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }

                override fun onFailure(call: Call<ForgetPasswordResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

            })
        }

        //resetPassword
        fun resetPassword(accessToken: String, resetPasswordRequest: ResetPasswordRequest, delegate: APIInterface.onDelegate) {
            val call: Call<ResetPasswordResponse> = getData().resetPassword(accessToken, resetPasswordRequest)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<ResetPasswordResponse> {
                override fun onFailure(call: Call<ResetPasswordResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<ResetPasswordResponse>,
                    response: Response<ResetPasswordResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //getSubscriptionType
        fun getSubscriptionType(delegate: APIInterface.onDelegate) {
            val call: Call<SubscriptionTypeResponse> = getData().getSubscriptionType()
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<SubscriptionTypeResponse> {
                override fun onFailure(call: Call<SubscriptionTypeResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<SubscriptionTypeResponse>,
                    response: Response<SubscriptionTypeResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //getSubscription
        fun getSubscription(subscriptionTypeID: String?, delegate: APIInterface.onDelegate) {
            val call: Call<SubscriptionTypeDetailResponse> = getData().getSubscription(subscriptionTypeID)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<SubscriptionTypeDetailResponse> {
                override fun onFailure(call: Call<SubscriptionTypeDetailResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<SubscriptionTypeDetailResponse>,
                    response: Response<SubscriptionTypeDetailResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //get repositories
        fun getRepositories(organizationId: String?, accessToken: String, delegate: APIInterface.onDelegate) {
            val call: Call<RepositoriesResponse> = getData().getRepositories(organizationId, accessToken)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<RepositoriesResponse> {
                override fun onFailure(call: Call<RepositoriesResponse>, t: Throwable) {

                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<RepositoriesResponse>,
                    response: Response<RepositoriesResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //get Workspace
        fun getWorkspace(repositoryId: String?, accessToken: String?, delegate: APIInterface.onDelegate) {
            val call: Call<WorkspaceResponse> = getData().getWorkspace(repositoryId, accessToken)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<WorkspaceResponse> {
                override fun onFailure(call: Call<WorkspaceResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<WorkspaceResponse>,
                    response: Response<WorkspaceResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //get Project Management
        fun getProjectManagement(workspaceId: String?, accessToken: String?, delegate: APIInterface.onDelegate) {
            val call: Call<ProjectManagementResponse> = getData().getProjectBoard(workspaceId, accessToken)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<ProjectManagementResponse> {
                override fun onFailure(call: Call<ProjectManagementResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<ProjectManagementResponse>,
                    response: Response<ProjectManagementResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //call create Organization
        fun createOrganization(accessToken: String?, organization: OrganizationRequest, delegate: APIInterface.onDelegate) {
            val call: Call<OrganizationResponse> = getData().createOrganization(accessToken, organization)
            call.enqueue(object : Callback<OrganizationResponse> {
                override fun onFailure(call: Call<OrganizationResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<OrganizationResponse>,
                    response: Response<OrganizationResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //call organization check
        fun organizationCheck(accessToken: String?, organization: OrganizationRequest, delegate: APIInterface.onDelegate) {
            val call: Call<OrganizationCheckResponse> = getData().organizationCheck(accessToken, organization)
            call.enqueue(object : Callback<OrganizationCheckResponse> {
                override fun onFailure(call: Call<OrganizationCheckResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<OrganizationCheckResponse>,
                    response: Response<OrganizationCheckResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //call join exist Organization
        fun organizationJoinExist(accessToken: String?, organization: OrganizationJoinExistRequest, delegate: APIInterface.onDelegate) {
            val call: Call<OrganizationResponse> = getData().organizationJoinExist(accessToken, organization)
            call.enqueue(object : Callback<OrganizationResponse> {
                override fun onFailure(call: Call<OrganizationResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<OrganizationResponse>,
                    response: Response<OrganizationResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //get Organization Detail
        fun getOrganizationDetail(accessToken: String?, organizationId: String?, delegate: APIInterface.onDelegate) {
            val call: Call<OrganizationUsersResponse> = getData().organizationDetail(accessToken, organizationId, 0, 10000)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<OrganizationUsersResponse> {
                override fun onFailure(call: Call<OrganizationUsersResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<OrganizationUsersResponse>,
                    response: Response<OrganizationUsersResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //call Repository
        fun createRepository(accessToken: String?, organizationId: String?, repositoryRequest: RepositoryRequest, delegate: APIInterface.onDelegate) {
            val call: Call<CreateRepositoryResponse> = getData().createRepository(accessToken, organizationId, repositoryRequest)
            call.enqueue(object : Callback<CreateRepositoryResponse> {
                override fun onFailure(call: Call<CreateRepositoryResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CreateRepositoryResponse>,
                    response: Response<CreateRepositoryResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //get user repository
        fun userRepository(accessToken: String?, repositoryId: String?, delegate: APIInterface.onDelegate) {
            val call: Call<UsersRepositoryResponse> = getData().userRepository(accessToken, repositoryId)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<UsersRepositoryResponse> {
                override fun onFailure(call: Call<UsersRepositoryResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<UsersRepositoryResponse>,
                    response: Response<UsersRepositoryResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //call workspace
        fun createWorkspace(accessToken: String?, organizationId: String?, workspaceRequest: WorkspaceRequest, delegate: APIInterface.onDelegate) {
            val call: Call<CreateWorkspaceResponse> = getData().createWorkspace(accessToken, organizationId, workspaceRequest)
            call.enqueue(object : Callback<CreateWorkspaceResponse> {
                override fun onFailure(call: Call<CreateWorkspaceResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CreateWorkspaceResponse>,
                    response: Response<CreateWorkspaceResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //call edit workspace
        fun editWorkspace(accessToken: String?, organizationId: String?, workspaceId: String?,
                          workspaceRequest: EditWorkspaceRequest, delegate: APIInterface.onDelegate) {
            val call: Call<EditWorkspaceResponse> = getData().editWorkspace(accessToken, organizationId, workspaceId, workspaceRequest)
            call.enqueue(object : Callback<EditWorkspaceResponse> {
                override fun onFailure(call: Call<EditWorkspaceResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<EditWorkspaceResponse>,
                    response: Response<EditWorkspaceResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //delete Workspace
        fun deleteWorkspace(accessToken: String?, organizationId: String?, workspaceId: String?, delegate: APIInterface.onDelegate) {
            val call: Call<CreateWorkspaceResponse> = getData().deleteWorkspace(accessToken, organizationId, workspaceId)

            call.enqueue(object : Callback<CreateWorkspaceResponse> {
                override fun onFailure(call: Call<CreateWorkspaceResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CreateWorkspaceResponse>,
                    response: Response<CreateWorkspaceResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //call project
        fun createProject(accessToken: String?, organizationId: String?, workspaceId: String?,
                          projectRequest: ProjectRequest, delegate: APIInterface.onDelegate) {
            val call: Call<CreateProjectResponse> = getData().createProject(accessToken, organizationId, workspaceId, projectRequest)
            call.enqueue(object : Callback<CreateProjectResponse> {
                override fun onFailure(call: Call<CreateProjectResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CreateProjectResponse>,
                    response: Response<CreateProjectResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //call project
        fun createBoard(accessToken: String?, organizationId: String?, workspaceFolderId: String?,
                          boardRequest: BoardRequest, delegate: APIInterface.onDelegate) {
            val call: Call<CreateBoardResponse> = getData().createBoard(accessToken, organizationId, workspaceFolderId, boardRequest)
            call.enqueue(object : Callback<CreateBoardResponse> {
                override fun onFailure(call: Call<CreateBoardResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CreateBoardResponse>,
                    response: Response<CreateBoardResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //call delete project
        fun deleteProject(accessToken: String?, organizationId: String?, folderId: String?, delegate: APIInterface.onDelegate) {
            val call: Call<CreateBoardResponse> = getData().deleteProject(accessToken, organizationId, folderId)
            call.enqueue(object : Callback<CreateBoardResponse> {
                override fun onFailure(call: Call<CreateBoardResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CreateBoardResponse>,
                    response: Response<CreateBoardResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //call delete project
        fun deleteBoard(accessToken: String?, organizationId: String?, boardId: String?, delegate: APIInterface.onDelegate) {
            val call: Call<CreateBoardResponse> = getData().deleteBoard(accessToken, organizationId, boardId)
            call.enqueue(object : Callback<CreateBoardResponse> {
                override fun onFailure(call: Call<CreateBoardResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CreateBoardResponse>,
                    response: Response<CreateBoardResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //view update Notification
        fun updateNotification(accessToken: String, notificationListRequest: NotificationListRequest, delegate: APIInterface.onDelegate) {
            val call: Call<CreateBoardResponse> = getData().updateNotification(accessToken, notificationListRequest)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<CreateBoardResponse> {
                override fun onFailure(call: Call<CreateBoardResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CreateBoardResponse>,
                    response: Response<CreateBoardResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //get notification
        fun getNotification(accessToken: String?, notificationState: String?, pageIndex: Int?,pageSize: Int?,
                            delegate: APIInterface.onDelegate) {
            val call: Call<NotificationResponse> = getData().getNotification(accessToken, notificationState, pageIndex, pageSize)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<NotificationResponse> {
                override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<NotificationResponse>,
                    response: Response<NotificationResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        fun handleError(response : Response<out Any>?, delegate: APIInterface.onDelegate, message: String? = null) {
            var error = ErrorModel()
            try {
                val errorResponse = Gson().fromJson(
                    response?.errorBody()!!.string(),
                    ErrorModel::class.java
                )
                errorResponse?.let {
                    if (errorResponse.message != null) {
                        error.message = errorResponse.message
                    } else {
                        errorResponse?.response?.let {
                            error.message = it
                        }
                    }
                    error.status = response.code().toString()
                }

                if (error.message == null) {
                    if (message != null) {
                        error.message = message
                    } else {
                        error.message = "Error"
                    }
                }
                delegate.onError(error)
            } catch (e: Exception) {
                if (response != null && response.message() != null) {
                    error.message = response.message()
                } else {
                    error.message = "Error"
                }
                message?.let {
                    error.message = message
                }

                delegate.onError(error)
            }
        }

        //edit tag name
        fun editTagName(accessToken: String, tagId: String, tagRequest: TagRequest, delegate: APIInterface.onDelegate) {
            val call: Call<TagsResponse> = getData().editTagName(accessToken, tagId, tagRequest)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<TagsResponse> {
                override fun onFailure(call: Call<TagsResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<TagsResponse>,
                    response: Response<TagsResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //edit tag name
        fun deleteTag(accessToken: String, tagId: String, delegate: APIInterface.onDelegate) {
            val call: Call<BaseResponse> = getData().deleteTag(accessToken, tagId)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<BaseResponse> {
                override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<BaseResponse>,
                    response: Response<BaseResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //add tag name
        fun addTag(accessToken: String, addTagRequest: AddTagRequest, delegate: APIInterface.onDelegate) {
            val call: Call<TagsResponse> = getData().addTag(accessToken, addTagRequest)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<TagsResponse> {
                override fun onFailure(call: Call<TagsResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<TagsResponse>,
                    response: Response<TagsResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //comment add tag
        fun commentAddTag(accessToken: String, tagId: String, commentId: String, delegate: APIInterface.onDelegate) {
            val call: Call<CommentTagResponse> = getData().commentAddTag(accessToken, tagId, commentId)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<CommentTagResponse> {
                override fun onFailure(call: Call<CommentTagResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CommentTagResponse>,
                    response: Response<CommentTagResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        //comment delete tag
        fun commentDeleteTag(accessToken: String, tagId: String, commentId: String, delegate: APIInterface.onDelegate) {
            val call: Call<BaseResponse> = getData().commentDeleteTag(accessToken, tagId, commentId)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<BaseResponse> {
                override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<BaseResponse>,
                    response: Response<BaseResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response)
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        fun bodyPose(accessToken: String, requestBody: RequestBody, delegate: APIInterface.onDelegate) {
            val call: Call<ResponseBody> = getData().bodyPose(accessToken, requestBody)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        fun removeRepository(accessToken: String, repositoryId: String? , organizationId: String?, delegate: APIInterface.onDelegate) {
            val call: Call<CreateRepositoryResponse> = getData().removeRepository(accessToken, repositoryId, organizationId)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<CreateRepositoryResponse> {
                override fun onFailure(call: Call<CreateRepositoryResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CreateRepositoryResponse>,
                    response: Response<CreateRepositoryResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        fun updateRepository(accessToken: String, repositoryId: String? , body: RepositoryRequest, delegate: APIInterface.onDelegate) {
            val call: Call<RepositoriesResponse> = getData().updateRepository(accessToken, body.organizationId, repositoryId, body)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<RepositoriesResponse> {
                override fun onFailure(call: Call<RepositoriesResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<RepositoriesResponse>,
                    response: Response<RepositoriesResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        fun photoUpdateRepository(accessToken: String, repositoryId: String?, repositoryPhotoAWSlink: PhotoRepositoryRequest, delegate: APIInterface.onDelegate) {
            val call: Call<CreateRepositoryResponse> = getData().photoUpdateRepository(accessToken,repositoryPhotoAWSlink.organizationId,
                repositoryId, repositoryPhotoAWSlink)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<CreateRepositoryResponse> {
                override fun onFailure(call: Call<CreateRepositoryResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<CreateRepositoryResponse>,
                    response: Response<CreateRepositoryResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        fun deleteUser(accessToken: String, organizationId: String, repositoryUserId: String, delegate: APIInterface.onDelegate) {
            val call: Call<DeleteRepositoriesResponse> = getData().deleteUser(accessToken, organizationId, organizationId, repositoryUserId)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<DeleteRepositoriesResponse> {
                override fun onFailure(call: Call<DeleteRepositoriesResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<DeleteRepositoriesResponse>,
                    response: Response<DeleteRepositoriesResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        fun addUser(accessToken: String, repositoryId: String?, body: AddUserRequest, delegate: APIInterface.onDelegate) {
            val call: Call<UserResponse> = getData().addUser(accessToken, body.organizationId, repositoryId, body)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<UserResponse> {
                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<UserResponse>,
                    response: Response<UserResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        fun serviceUpload(accessToken: String, requestBody: RequestBody, delegate: APIInterface.onDelegate) {
            val call: Call<ResponseBody> = getData().serviceUpload(accessToken, requestBody)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }

        fun getOrganizationByName(organization: OrganizationByNameRequest,token:String?, delegate: APIInterface.onDelegate) {
            val call: Call<OrganizationByNameResponse> = getData().getOrganizationByName(token,organization)
            Log.d(TAG, "URL: ${call.request().url}")
            call.enqueue(object : Callback<OrganizationByNameResponse> {
                override fun onFailure(call: Call<OrganizationByNameResponse>, t: Throwable) {
                    handleError(null, delegate, t.message)
                }

                override fun onResponse(
                    call: Call<OrganizationByNameResponse>,
                    response: Response<OrganizationByNameResponse>
                ) {
                    if (response.isSuccessful) {
                        delegate.onSuccess(response.body())
                    } else {
                        handleError(response, delegate)
                    }
                }
            })
        }
    }
}