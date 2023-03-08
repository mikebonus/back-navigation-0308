package com.luxpmsoft.luxaipoc.api

import com.luxpmsoft.luxaipoc.model.BaseResponse
import com.luxpmsoft.luxaipoc.model.RequestSessionResponse
import com.luxpmsoft.luxaipoc.model.FinishUploadPhotoResponse
import com.luxpmsoft.luxaipoc.model.comment.CommentRequest
import com.luxpmsoft.luxaipoc.model.comment.CommentResponse
import com.luxpmsoft.luxaipoc.model.defect_detect.DefectDetectResponse
import com.luxpmsoft.luxaipoc.model.defect_detect.ExerciseCategoryResponse
import com.luxpmsoft.luxaipoc.model.defect_detect.TestedModelsResponse
import com.luxpmsoft.luxaipoc.model.defect_detect.TrainedModelResponse
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
import com.luxpmsoft.luxaipoc.model.repositories.request.RemoveRepositoryRequest
import com.luxpmsoft.luxaipoc.model.repositories.request.RepositoryRequest
import com.luxpmsoft.luxaipoc.model.select.Model3DResponse
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionRequest
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionTypeDetailResponse
import com.luxpmsoft.luxaipoc.model.subscription.SubscriptionTypeResponse
import com.luxpmsoft.luxaipoc.model.user.*
import com.luxpmsoft.luxaipoc.model.workout.WorkoutResponse
import com.luxpmsoft.luxaipoc.model.workspaces.CreateWorkspaceResponse
import com.luxpmsoft.luxaipoc.model.workspaces.EditWorkspaceResponse
import com.luxpmsoft.luxaipoc.model.workspaces.WorkspaceResponse
import com.luxpmsoft.luxaipoc.model.workspaces.request.EditWorkspaceRequest
import com.luxpmsoft.luxaipoc.model.workspaces.request.WorkspaceRequest
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface APIService {
    @POST(ConstantAPI.UPLOAD_FILE)
    fun uploadFile(@Body profile: RequestBody): Call<String>

    @GET(ConstantAPI.ALL_MODELS)
    fun getAllModels(
    ): Call<Model3DResponse>

    @GET(ConstantAPI.ALL_MODELS)
    fun getArAllModels(
        @Query("user_id") user_id: String?
    ): Call<Model3DResponse>

    @GET(ConstantAPI.VIEW_MODELS)
    fun viewModel(
        @Query("session_id") session_id: String?
    ): Call<ResponseBody>

    @GET(ConstantAPI.VIEW_MODELS)
    fun viewArModel(
        @Query("user_id") user_id: String?,
        @Query("session_id") session_id: String?
    ): Call<ResponseBody>

    @GET(ConstantAPI.GET_MODEL_URL)
    fun getModelUrl(
        @Query("user_id") user_id: String?,
        @Query("session_id") session_id: String?
    ): Call<String>

    @GET(ConstantAPI.GET_THUMBNAIL)
    fun getThumbnail(
        @Query("user_id") user_id: String?,
        @Query("session_id") session_id: String?
    ): Call<String>

    @GET(ConstantAPI.RENAME_MODEL)
    fun renameModel(
        @Query("user_id") user_id: String?,
        @Query("session_id") session_id: String?,
        @Query("new_session_id") new_session_id: String?
    ): Call<ResponseBody>

    @POST(ConstantAPI.REQUEST_SESSION)
    fun requestSession(
        @Body body: RequestBody
    ): Call<RequestSessionResponse>

    @POST(ConstantAPI.UPLOAD_PHOTO)
    fun uploadPhoto(
        @Body body: RequestBody): Call<ResponseBody>

    @POST(ConstantAPI.MULTI_UPLOAD_PHOTO)
    fun uploadMultiPhoto(
        @Body body: RequestBody): Call<ResponseBody>

    @POST(ConstantAPI.MULTI_UPLOAD_VIDEO)
    fun uploadMultiVideo(
        @Body body: RequestBody): Call<ResponseBody>

    @POST(ConstantAPI.UPLOAD_FILE_ZIP)
    fun uploadFileZip(
        @Query("production_flag") production_flag: String?,
        @Body body: RequestBody): Call<ResponseBody>

    @GET(ConstantAPI.FINISH_PHOTO_UPLOAD)
    fun finishUploadPhoto(
        @Query("user_id") user_id: String?,
        @Query("session_id") session_id: String?
    ): Call<FinishUploadPhotoResponse>

    @POST(ConstantAPI.LOGIN)
    fun login(@Body login: LoginRequest): Call<LoginResponse>

    @POST(ConstantAPI.LOGIN_SNS)
    fun loginSNS(@Body login: LoginSNSRequest): Call<LoginResponse>

    /*open air*/
    @GET(ConstantAPI.SUBSCRIPTION_USER)
    fun getSubscriptionUser(
        @Header("x-access-token") accessToken: String?
    ): Call<SubscriptionResponse>

    @GET(ConstantAPI.DASHBOARD)
    fun getDashboard(
        @Path("organizationId") organizationId: String?,
        @Header("x-access-token") accessToken: String?
    ): Call<OrganizationDashboardResponse>

    @POST(ConstantAPI.SUBSCRIPTION_USER)
    fun addSubscriptionUser(@Header("x-access-token") accessToken: String?,
                            @Body add: SubscriptionRequest): Call<SubscriptionResponse>

    @GET(ConstantAPI.RECONSTRUCTION)
    fun getReconstruction(
        @Header("x-access-token") accessToken: String?,
        @Query("pageIndex") pageIndex: Int?,
        @Query("pageSize") pageSize: Int?,
        @Query("filter") filter: String?,
        @Query("sort") sort: String?,
        @Query("sortType") sortType: String?,
        @Query("search") search: String?,
        @Query("organizationID") organizationID: String?
        ): Call<ReconstructionResponse>

    @POST(ConstantAPI.RECONSTRUCTION)
    fun sceneReconstruction(
        @Header("x-access-token") accessToken: String?,
        @Body body: RequestBody): Call<ResponseBody>

    @PATCH(ConstantAPI.RECONSTRUCTION)
    fun renameModel(
        @Header("x-access-token") accessToken: String?,
        @Body body: RenameModelRequest
    ): Call<RenameModelResponse>

    @DELETE(ConstantAPI.RECONSTRUCTION)
    fun deleteModel(
        @Header("x-access-token") accessToken: String?,
        @Query("reconstructionID") reconstructionID: String?
    ): Call<BaseResponse>

    @GET(ConstantAPI.CAD_FILE)
    fun getCadFile(
        @Header("x-access-token") accessToken: String?,
        @Path("boardId") boardId: String?,
        @Query("pageIndex") pageIndex: Int?,
        @Query("pageSize") pageSize: Int?,
        @Query("search") search: String?
        ): Call<CadFileResponse>

    @GET(ConstantAPI.CAD_FILE_DETAIL)
    fun getCadFileDetail(
        @Header("x-access-token") accessToken: String?,
        @Path("id") id: String?
    ): Call<CadFileDetailResponse>

    @DELETE(ConstantAPI.CAD_FILE_DETAIL)
    fun deleteFile(
        @Header("x-access-token") accessToken: String?,
        @Path("id") id: String?
    ): Call<CreateBoardResponse>

    @POST(ConstantAPI.CAD_FILE_ADD)
    fun addCadFile(
        @Header("x-access-token") accessToken: String?,
        @Header("OrganizationId") OrganizationId: String?,
        @Body body: CadFileRequest
    ): Call<CadFileResponse>

    @PUT(ConstantAPI.CAD_FILE_SAVE)
    fun saveCadFile(
        @Header("x-access-token") accessToken: String?,
        @Path("cadFileId") cadFileId: String?,
        @Body body: SaveCadFileRequest
    ): Call<CadFileResponse>

    @GET(ConstantAPI.RECONSTRUCTION_HTML)
    fun getReconstructionHTML(
        @Header("x-access-token") accessToken: String?,
        @Path("id") id: String?
    ): Call<ResponseBody>

    @GET(ConstantAPI.MODELS)
    fun getModel(
        @Header("x-access-token") accessToken: String?,
        @Path("reconstructionID") reconstructionID: String?
    ): Call<ResponseBody>

    @GET(ConstantAPI.USER_DETAIL)
    fun getUserDetail(
        @Header("x-access-token") accessToken: String?
    ): Call<UserDetailResponse>

    @GET(ConstantAPI.ME)
    fun getMe(
        @Header("x-access-token") accessToken: String?
    ): Call<MeResponse>

    @PUT(ConstantAPI.USER_DETAIL)
    fun updateUser(
        @Header("x-access-token") accessToken: String?,
        @Body user: UpdateUserRequest
    ): Call<UserImageResponse>

    @PUT(ConstantAPI.USER_IMAGE)
    fun uploadAvatar(
        @Header("x-access-token") accessToken: String?,
        @Body body: RequestBody): Call<UserImageResponse>

    @POST(ConstantAPI.COMMENT_ADD)
    fun addComment(@Header("x-access-token") accessToken: String?,
                   @Header("OrganizationId") OrganizationId: String?,
                   @Body body: CommentRequest): Call<CommentResponse>

    @GET(ConstantAPI.COMMENT_GET)
    fun getComment(
        @Header("x-access-token") accessToken: String?,
        @Path("commentId") commentId: String?
    ): Call<CommentResponse>

    @POST(ConstantAPI.SEND_OTP)
    fun sendOTP(@Body sendOTP: SendOTPRequest): Call<SendOTPResponse>

    @POST(ConstantAPI.REGISTER)
    fun register(@Body register: RegisterRequest): Call<RegisterResponse>

    @POST(ConstantAPI.EMAIL_VERIFICATION)
    fun emailVerification(@Body emailVerification: EmailVerificationRequest): Call<EmailVerificationResponse>

    @POST(ConstantAPI.RESET_SEND_OTP)
    fun resetSendOTP(@Body sendOTP: SendOTPRequest): Call<SendOTPResponse>

    @POST(ConstantAPI.VERIFY_OTP)
    fun verifyOTP(@Body verify: VerifyOTPRequest): Call<VerifyOTPResponse>

    @PUT(ConstantAPI.RESET_PASSWORD)
    fun resetPassword(
        @Header("x-access-token") accessToken: String?,
        @Body reset: ResetPasswordRequest
    ): Call<ResetPasswordResponse>

    @PUT(ConstantAPI.CHANGE_PASSWORD)
    fun changePassword(
        @Header("x-access-token") accessToken: String?,
        @Body changePassword: ChangePasswordRequest
    ): Call<ResetPasswordResponse>

    @POST(ConstantAPI.FORGET_PASSWORD)
    fun forgetPassword(
        @Header("x-access-token") accessToken: String?,
        @Body forgetPasswordRequest: ForgetPasswordRequest
    ): Call<ForgetPasswordResponse>

    @GET(ConstantAPI.SUBSCRIPTION_TYPE)
    fun getSubscriptionType(): Call<SubscriptionTypeResponse>

    @GET(ConstantAPI.SUBSCRIPTION)
    fun getSubscription(
        @Query("subscriptionTypeID") subscriptionTypeID: String?
    ): Call<SubscriptionTypeDetailResponse>

//    @Multipart
//    @POST(ConstantAPI.UPLOAD_PHOTO)
//    fun uploadPhoto(@PartMap map: HashMap<String?, RequestBody?>): Call<ResponseBody>

    @GET(ConstantAPI.ORGANIZATION_REPOSITORIES)
    fun getRepositories(
        @Path("organizationId") organizationId: String?,
        @Header("x-access-token") accessToken: String?
    ): Call<RepositoriesResponse>

    @GET(ConstantAPI.ORGANIZATION_WORKSPACE)
    fun getWorkspace(
        @Path("repositoryId") repositoryId: String?,
        @Header("x-access-token") accessToken: String?
    ): Call<WorkspaceResponse>

    @GET(ConstantAPI.ORGANIZATION_PROJECT_BOARD)
    fun getProjectBoard(
        @Path("workspaceId") workspaceId: String?,
        @Header("x-access-token") accessToken: String?
    ): Call<ProjectManagementResponse>

    @POST(ConstantAPI.ORGANIZATION_CREATE)
    fun createOrganization(
        @Header("x-access-token") accessToken: String?,
        @Body body: OrganizationRequest
    ): Call<OrganizationResponse>

    @POST(ConstantAPI.ORGANIZATION_CHECK)
    fun organizationCheck(
        @Header("x-access-token") accessToken: String?,
        @Body body: OrganizationRequest
    ): Call<OrganizationCheckResponse>

    @POST(ConstantAPI.ORGANIZATION_JOIN_REQUEST)
    fun organizationJoinExist(
        @Header("x-access-token") accessToken: String?,
        @Body body: OrganizationJoinExistRequest
    ): Call<OrganizationResponse>

    @GET(ConstantAPI.ORGANIZATION_DETAIL)
    fun organizationDetail(
        @Header("x-access-token") accessToken: String?,
        @Path("organizationId") organizationId: String?,
        @Query("pageIndex") pageIndex: Int?,
        @Query("pageSize") pageSize: Int?): Call<OrganizationUsersResponse>

    @GET(ConstantAPI.USER_REPOSITORY)
    fun userRepository(
        @Header("x-access-token") accessToken: String?,
        @Path("repositoryId") organizationId: String?
    ): Call<UsersRepositoryResponse>


    @POST(ConstantAPI.CREATE_REPOSITORY)
    fun createRepository(
        @Header("x-access-token") accessToken: String?,
        @Header("OrganizationId") OrganizationId: String?,
        @Body body: RepositoryRequest): Call<CreateRepositoryResponse>

    @POST(ConstantAPI.CREATE_WORKSPACE)
    fun createWorkspace(
        @Header("x-access-token") accessToken: String?,
        @Header("OrganizationId") OrganizationId: String?,
        @Body body: WorkspaceRequest): Call<CreateWorkspaceResponse>

    @PUT(ConstantAPI.EDIT_WORKSPACE)
    fun editWorkspace(
        @Header("x-access-token") accessToken: String?,
        @Header("OrganizationId") OrganizationId: String?,
        @Path("workspaceId") workspaceId: String?,
        @Body body: EditWorkspaceRequest
    ): Call<EditWorkspaceResponse>

    @DELETE(ConstantAPI.DELETE_WORKSPACE)
    fun deleteWorkspace(
        @Header("x-access-token") accessToken: String?,
        @Path("OrganizationId") OrganizationId: String?,
        @Path("workspaceId") workspaceId: String?
    ): Call<CreateWorkspaceResponse>

    @POST(ConstantAPI.CREATE_PROJECT)
    fun createProject(
        @Header("x-access-token") accessToken: String?,
        @Header("OrganizationId") OrganizationId: String?,
        @Path("workspaceId") workspaceId: String?,
        @Body body: ProjectRequest
    ): Call<CreateProjectResponse>

    @POST(ConstantAPI.CREATE_BOARD)
    fun createBoard(
        @Header("x-access-token") accessToken: String?,
        @Header("OrganizationId") OrganizationId: String?,
        @Path("workspaceFolderId") workspaceFolderId: String?,
        @Body body: BoardRequest
    ): Call<CreateBoardResponse>

    @DELETE(ConstantAPI.DELETE_PROJECT)
    fun deleteProject(
        @Header("x-access-token") accessToken: String?,
        @Header("OrganizationId") OrganizationId: String?,
        @Path("folderId") folderId: String?
    ): Call<CreateBoardResponse>

    @DELETE(ConstantAPI.DELETE_BOARD)
    fun deleteBoard(
        @Header("x-access-token") accessToken: String?,
        @Header("OrganizationId") OrganizationId: String?,
        @Path("boardId") boardId: String?
    ): Call<CreateBoardResponse>

    @GET(ConstantAPI.NOTIFICATION)
    fun getNotification(
        @Header("x-access-token") accessToken: String?,
        @Query("notificationState") notificationState: String?,
        @Query("pageIndex") pageIndex: Int?,
        @Query("pageSize") pageSize: Int?
    ): Call<NotificationResponse>

    @PATCH(ConstantAPI.NOTIFICATION)
    fun updateNotification(
        @Header("x-access-token") accessToken: String?,
        @Body body: NotificationListRequest
    ): Call<CreateBoardResponse>

    @PUT(ConstantAPI.TAG_EDIT)
    fun editTagName(
        @Header("x-access-token") accessToken: String?,
        @Path("tagId") tagId: String?,
        @Body tagRequest: TagRequest
    ): Call<TagsResponse>

    @DELETE(ConstantAPI.TAG_EDIT)
    fun deleteTag(
        @Header("x-access-token") accessToken: String?,
        @Path("tagId") tagId: String?
    ): Call<BaseResponse>

    @POST(ConstantAPI.TAG_ADD)
    fun addTag(
        @Header("x-access-token") accessToken: String?,
        @Body tagRequest: AddTagRequest
    ): Call<TagsResponse>

    @POST(ConstantAPI.COMMENT_TAG_ADD)
    fun commentAddTag(
        @Header("x-access-token") accessToken: String?,
        @Path("tagId") tagId: String?,
        @Path("commentId") commentId: String?
    ): Call<CommentTagResponse>

    @DELETE(ConstantAPI.COMMENT_TAG_ADD)
    fun commentDeleteTag(
        @Header("x-access-token") accessToken: String?,
        @Path("tagId") tagId: String?,
        @Path("commentId") commentId: String?
    ): Call<BaseResponse>

    @POST(ConstantAPI.CREATE_SESSION)
    fun createSession(
    ): Call<RequestSessionResponse>

    @POST(ConstantAPI.UPLOAD_VIDEO)
    fun uploadVideo(
        @Body body: RequestBody): Call<ResponseBody>

    @GET(ConstantAPI.DOWNLOAD_SESSION)
    fun downloadBody(
        @Path("session_id") session_id: String?
    ): Call<String>

    @POST(ConstantAPI.BODY_POSE)
    fun bodyPose(
        @Header("x-access-token") accessToken: String?,
        @Body body: RequestBody): Call<ResponseBody>

    //new
    @DELETE(ConstantAPI.REMOVE_REPOSITORIES)
    fun removeRepository(
        @Header("x-access-token") accessToken: String?,
        @Path("repositoryId") repositoryId: String?,
        @Header("OrganizationId") OrganizationId: String?): Call<CreateRepositoryResponse>

    @PUT(ConstantAPI.UPDATE_REPOSITORIES)
    fun updateRepository(
        @Header("x-access-token") accessToken: String?,
        @Header("OrganizationId") OrganizationId: String?,
        @Path("repositoryId") repositoryId: String?,
        @Body body: RepositoryRequest
    ): Call<RepositoriesResponse>

    @PUT(ConstantAPI.PHOTO_UPDATE_REPOSITORIES)
    fun photoUpdateRepository(
        @Header("x-access-token") accessToken: String?,
        @Header("OrganizationId") OrganizationId: String?,
        @Path("repositoryId") repositoryId: String?,
        @Body body: PhotoRepositoryRequest
    ): Call<CreateRepositoryResponse>

    @DELETE(ConstantAPI.DELETE_USER)
    fun deleteUser(
        @Header("x-access-token") accessToken: String?,
        @Header("OrganizationId") OrganizationId: String?,
        @Path("organizationId") organizationId: String?,
        @Path("repositoryUserId") repositoryUserId: String?
    ): Call<DeleteRepositoriesResponse>

    @POST(ConstantAPI.ADD_USER)
    fun addUser(
        @Header("x-access-token") accessToken: String?,
        @Header("OrganizationId") OrganizationId: String?,
        @Path("repositoryId") repositoryId: String?,
        @Body body: AddUserRequest
    ): Call<UserResponse>

    @POST(ConstantAPI.SERVICE_UPLOAD)
    fun serviceUpload(
        @Header("x-access-token") accessToken: String?,
        @Body body: RequestBody): Call<ResponseBody>

    //defect detect
    @GET(ConstantAPI.DEFECT_GET_MODEL)
    fun getTrainedModel(
        @Header("x-access-token") accessToken: String?,
        @Path("userId") userId: String?,
        @Query("pageIndex") pageIndex: Int?,
        @Query("pageSize") pageSize: Int?
    ): Call<TrainedModelResponse>

    @POST(ConstantAPI.TEST_DETECTION)
    fun testMultiDetection(
        @Header("x-access-token") accessToken: String?,
        @Body body: RequestBody): Call<DefectDetectResponse>

    @POST(ConstantAPI.INCREASE_DATASET)
    fun increaseDataset(
        @Header("x-access-token") accessToken: String?,
        @Body body: RequestBody): Call<ResponseBody>

    @POST(ConstantAPI.GENERATE_SESSION)
    fun generateSession(
        @Header("x-access-token") accessToken: String?,
        @Body body: RequestBody): Call<ResponseBody>

    @POST(ConstantAPI.RETRAIN_PROCESS)
    fun retrainProcess(
        @Header("x-access-token") accessToken: String?,
        @Body body: RequestBody): Call<ResponseBody>

    @POST(ConstantAPI.DOWNLOAD_ZIP)
    fun downloadZip(
        @Query("file_path") file_path: String?): Call<ResponseBody>

    @GET(ConstantAPI.GET_TESTED_MODELS)
    fun getTestedModels(
        @Header("x-access-token") accessToken: String?,
        @Path("user_id") userId: String?,
        @Query("session_id") session_id: String?,
        @Query("pageIndex") pageIndex: Int?,
        @Query("pageSize") pageSize: Int?
    ): Call<TestedModelsResponse>

    //workout
    @POST(ConstantAPI.WORKOUT_VIDEO)
    fun workoutVideo(
        @Header("x-access-token") accessToken: String?,
        @Body body: RequestBody): Call<ResponseBody>

    @GET(ConstantAPI.GET_EXERCISE_VIDEOS)
    fun getExerciseVideo(
        @Header("x-access-token") accessToken: String?,
        @Query("pageSize") pageSize: Int?,
        @Query("pageLimit") pageLimit: Int?
    ): Call<WorkoutResponse>

    @GET(ConstantAPI.GET_EXERCISE_CATEGORY)
    fun getExerciseCategory(
        @Header("x-access-token") accessToken: String?,
        @Query("pageSize") pageSize: Int?,
        @Query("pageLimit") pageLimit: Int?
    ): Call<ExerciseCategoryResponse>

    @POST(ConstantAPI.GET_ORGANIZATION_BY_NAME)
    fun getOrganizationByName(
        @Header("x-access-token") accessToken: String?,
        @Body body: OrganizationByNameRequest,
    ): Call<OrganizationByNameResponse>
}