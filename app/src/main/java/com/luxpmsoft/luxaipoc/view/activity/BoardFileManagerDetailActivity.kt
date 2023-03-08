package com.luxpmsoft.luxaipoc.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.*
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.BuildConfig
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.CommentAdapter
import com.luxpmsoft.luxaipoc.adapter.TagsAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.BaseResponse
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.comment.CommentRequest
import com.luxpmsoft.luxaipoc.model.comment.DeleteCommentRequest
import com.luxpmsoft.luxaipoc.model.recentmodel.*
import com.luxpmsoft.luxaipoc.model.recentmodel.request.AddTagRequest
import com.luxpmsoft.luxaipoc.model.recentmodel.request.PositionRequest
import com.luxpmsoft.luxaipoc.model.recentmodel.request.TagRequest
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.widget.DialogFactory
import com.luxpmsoft.luxaipoc.widget.SwipeHelper
import dev.romainguy.kotlin.math.lookAt
import io.github.sceneview.math.Direction
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.LoadingNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.Transport
import kotlinx.android.synthetic.main.activity_board_file_manager_detail.*
import kotlinx.android.synthetic.main.item_comment.*
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.net.URISyntaxException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.*
import javax.net.ssl.*
import kotlin.math.round

class BoardFileManagerDetailActivity: BaseActivity(), CommentAdapter.OnListener, TagsAdapter.OnListener {
    var commentAdapter: CommentAdapter? = null
    var commentModel: ArrayList<Comments>? = ArrayList()
    var tagsAdapter: TagsAdapter? = null
    var tagsCommentAdapter: TagsAdapter? = null
    var tagsModel: ArrayList<Tags>? = ArrayList()
    var reconstructions: Array<Rows>? = null
    var cadFileID = ""
    var htmlFile = ""
    var modelName = ""
    var commentId = ""
    var index = 0
    var isLoadHtml = false
    var isReply = false
    var cadFile: CadFileRows? = null
    var socketClient: Socket?= null
    var popupWindow: PopupWindow? = null
    var p1: Vector3? = null
    var p2: Vector3? = null
    var listTag: ArrayList<TagNode>? = ArrayList()
    var modelNode: ModelNode? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        setContentView(R.layout.activity_board_file_manager_detail)
        init()
        listener()
    }

    fun init() {
        Utils.gridLayoutManager(this, grvComment, 1, GridLayoutManager.VERTICAL)
        commentAdapter = CommentAdapter(this, R.layout.item_comment, commentModel!!, this, true)
        grvComment.adapter = commentAdapter
//        Utils.gridLayoutManager(this@BoardFileManagerDetailActivity, grvTags, 1, GridLayoutManager.VERTICAL)
//        tagsAdapter = TagsAdapter(this@BoardFileManagerDetailActivity, R.layout.item_tag, tagsModel!!,
//            this@BoardFileManagerDetailActivity, false)
//        grvTags.adapter = tagsAdapter
        val bundle = intent.extras
        bundle?.let {
            cadFileID = it.getString("cadFileID").toString()
        }
        getCadFileDetail(cadFileID)
        MyUtils.loadAvatar(
            this, (application as LidarApp).prefManager?.getToken(),
            (application as LidarApp).prefManager?.getProfileImageKey(), avatar,
            resources.getDrawable(R.drawable.user))
        try {
            initSocket()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var swipeHelper = object : SwipeHelper(this, grvComment, false) {
            override fun instantiateUnderlayButton(
                viewHolder: RecyclerView.ViewHolder,
                underlayButtons: MutableList<UnderlayButton>
            ) {
                // Archive Button
                underlayButtons?.add(SwipeHelper.UnderlayButton(
                    "Archive",
                    getDrawable(R.drawable.ic_delete),
                    Color.parseColor("#FF0000"), Color.parseColor("#ffffff"),
                    UnderlayButtonClickListener { pos: Int ->
                        DialogFactory.dialogConfirmDelete(
                            this@BoardFileManagerDetailActivity,
                            object : DialogFactory.Companion.DialogListener.Delete {
                                override fun delete() {
                                    commentModel?.get(pos)?.commentId?.let { deleteComment(it) }
                                }
                            }, " ".plus(commentModel?.get(pos)?.text))
                    }
                ))
            }
        }
    }

    fun listener() {
        icBack.setOnClickListener {
            finish()
        }

        tvFullModel.setOnClickListener {
            runOnUiThread {
//                reconstructions?.let {
//                    it[0].reconstructionID?.let { it1 ->
//                        DialogFactory.dialogShowFullModel(
//                            this, htmlFile, reconstructions, isLoadHtml)
//                    }
//                }
                val intent = Intent(this, ShowFullModelActivity::class.java)
                intent.putExtra("htmlFile", htmlFile)
                intent.putExtra("cadFile", Gson().toJson(cadFile))
                intent.putExtra("isLoadHtml", isLoadHtml)
                startActivity(intent)
            }
        }

        lineSend.setOnClickListener {
            if (edtChatMessage.text.toString().trim().isEmpty()) {
                Toast.makeText(
                    this@BoardFileManagerDetailActivity,
                    "Please add some text",
                    Toast.LENGTH_LONG
                ).show()
            }
            if (edtChatMessage.text.toString().trim().isNotEmpty()) {
                addComment(cadFileID)
            }
        }

        tvComments.setOnClickListener {
//            lineComment.visibility = View.VISIBLE
//            lineTags.visibility = View.GONE
//            lineTypeComment.visibility = View.VISIBLE
//            distinguish("comment")
        }

        tvTags.setOnClickListener {
//            lineComment.visibility = View.GONE
//            lineTags.visibility = View.VISIBLE
//            lineTypeComment.visibility = View.GONE
//            distinguish("tag")
//            if (tagsModel!!.isNotEmpty()) {
//                showPopupWindow(tvTags, false, resources.getDimension(R.dimen.size_200).toInt())
//            } else {
//                Toast.makeText(this, "Tags empty", Toast.LENGTH_SHORT).show()
//            }
        }

        lineResetMeasurement.setOnClickListener {
        }
    }

    fun distinguish(type: String?) {
        when (type) {
            "comment" -> {
                tvComments.background = resources.getDrawable(R.drawable.bg_choose_24)
                tvTags.background = resources.getDrawable(R.drawable.bg_grey_24)
            }
            "tag" -> {
                tvComments.background = resources.getDrawable(R.drawable.bg_grey_24)
                tvTags.background = resources.getDrawable(R.drawable.bg_choose_24)
            }
        }
    }

    fun initSocket() {
        try {
            val mySSLContext: SSLContext = SSLContext.getInstance("TLS")
            mySSLContext.init(null, MyUtils.trustAllCerts, SecureRandom())
            val okHttpClient: OkHttpClient = OkHttpClient.Builder()
                .hostnameVerifier(MyUtils.myHostnameVerifier)
                .sslSocketFactory(
                    mySSLContext.getSocketFactory(),
                    MyUtils.trustAllCerts[0] as X509TrustManager
                )
                .build()
            IO.setDefaultOkHttpWebSocketFactory(okHttpClient)
            IO.setDefaultOkHttpCallFactory(okHttpClient)
            val opts = IO.Options()
            opts.callFactory = okHttpClient
            opts.webSocketFactory = okHttpClient
            opts.timeout = (60 * 1000).toLong()
            opts.forceNew = false
            opts.secure = true
            opts.reconnection = true
            opts.query = "x-access-token:" + (application as LidarApp).prefManager!!.getToken()
            opts.transports = arrayOf(
                "websocket"
            )
            socketClient = IO.socket(BuildConfig.URL, opts)
            socketClient?.connect()

            // Adding authentication headers when encountering EVENT_TRANSPORT
            socketClient?.io()?.on(Manager.EVENT_TRANSPORT, Emitter.Listener { args ->
                val transport: Transport = args[0] as Transport
                // Adding headers when EVENT_REQUEST_HEADERS is called
                transport.on(Transport.EVENT_REQUEST_HEADERS, Emitter.Listener { args ->
                    val mHeaders = args[0] as MutableMap<String, List<String>>
                    mHeaders["x-access-token"] = Arrays.asList((application as LidarApp).prefManager!!.getToken())
                })
            })
            socketClient?.on(Socket.EVENT_CONNECT,
                Emitter.Listener {
                    Log.e("EVENT_CONNECT", "socket connected")
                })?.on(Socket.EVENT_DISCONNECT,
                Emitter.Listener {
                    Log.e("EVENT_DISCONNECT", "socket disconnected")
                })?.on(Socket.EVENT_CONNECT_ERROR,
                Emitter.Listener {
                    Log.e("EVENT_CONNECT_ERROR", "socket error")
                })
            getComment()
            cadfileUpdates()
            socketClient?.emit("get_comment", {})

        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
//            sceneView.resume()
            sceneView.setBackgroundDrawable(resources.getDrawable(R.drawable.sceneview_info_background))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (socketClient?.connected() != true) {
            initSocket()
        }
        if (isReply) {
            getCadFileDetail(cadFileID)
        }
    }

    override fun onPause() {
        super.onPause()
//        sceneView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        commentModel?.let {
            commentModel = null
        }
        reconstructions?.let {
            reconstructions = null
        }
//        sceneView?.let {
////            it.pause()
////            it.destroy()
//        }
        socketClient?.disconnect()
        socketClient?.off("get_comment")
        socketClient?.off("cadfile_updates")
    }

    private fun getComment() {
        socketClient?.on("get_comment", Emitter.Listener { args ->
        })
    }

    private fun cadfileUpdates() {
        socketClient?.on("cadfile_updates", Emitter.Listener { args ->
            try {
                val messageJson = JSONObject(args[0].toString())
                val obj: CadFileDetailResponse = Gson().fromJson(messageJson.toString(), CadFileDetailResponse::class.java)
                runOnUiThread {
                    obj.response?.let {
                        if (it.cadFileID == cadFileID) {
                            it.comments?.let {
                                commentModel?.clear()
                                commentModel?.addAll(it)
                                commentAdapter?.notifyDataSetChanged()
                                grvComment.scrollToPosition(it.size-1)
                            }

                            it.tags?.let {
                                for (tag in listTag!!) {
                                    sceneView.removeChild(tag.node)
                                }
                                tagsModel?.clear()
                                listTag?.clear()
                                tagsModel?.addAll(it)
                                for (tag in it) {
                                    tag.position?.x?.let {
                                        val viewDistance = LoadingNode(position = Position(x = tag.position?.x!!.toFloat(),
                                            y = tag.position?.y!!.toFloat(), z = tag.position?.z!!.toFloat()+0.14f),
                                            lifecycle = lifecycle, layoutResId = R.layout.text_node, context = this,
                                            textNode = tag.name)
                                        modelNode?.addChild(viewDistance)
                                        listTag?.add(TagNode(tag.tagId, viewDistance))
                                    }
                                }

                                tagsAdapter?.notifyDataSetChanged()
//                                grvTags.scrollToPosition(it.size-1)
//                                showHideTag()
                            }
                        }

                        edtChatMessage.setText("")
                        commentModel?.let {
                            if (it.isNotEmpty()) {
                                grvComment.visibility = View.VISIBLE
                                tvEmptyComment.visibility = View.GONE
                            } else {
                                grvComment.visibility = View.GONE
                                tvEmptyComment.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        })
    }

    private fun showPopupWindow(anchor: View?, isShow:Boolean?, width: Int) {
        PopupWindow(anchor?.context).apply {
            isOutsideTouchable = true
            val inflater = LayoutInflater.from(anchor?.context)
            contentView = inflater.inflate(R.layout.dialog_tags, null).apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
            }

            var tag = contentView.findViewById<RecyclerView>(R.id.grvTags)
            Utils.gridLayoutManager(this@BoardFileManagerDetailActivity, tag, 1, GridLayoutManager.VERTICAL)
            tagsCommentAdapter = TagsAdapter(this@BoardFileManagerDetailActivity, R.layout.item_tag_comments, tagsModel!!,
                this@BoardFileManagerDetailActivity, isShow)
            tag.adapter = tagsCommentAdapter

        }.also { popupWindow ->
            this.popupWindow = popupWindow
            popupWindow.width = width
            popupWindow.height = LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            popupWindow.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )

            // Absolute location of the anchor view
            val location = IntArray(2).apply {
                anchor?.getLocationOnScreen(this)
            }
            val size = Size(
                popupWindow.contentView.measuredWidth,
                popupWindow.contentView.measuredHeight
            )
            popupWindow.showAtLocation(
                anchor,
                Gravity.TOP or Gravity.END,
                resources.getDimensionPixelOffset(R.dimen.size_26),
                location[1] - size.height+resources.getDimensionPixelOffset(R.dimen.size_26)+popupWindow.contentView.measuredHeight
            )
            if (isShow == false) {
                popupWindow.setFocusable(true)
                popupWindow.update()
            }
        }
    }

    override fun onListener(comment: Comments) {
        isReply = true
        socketClient?.disconnect()
        val intent = Intent(this, ReplyChatActivity::class.java)
        intent.putExtra("comment", Gson().toJson(comment))
        startActivity(intent)
    }

    override fun onAddTagComment(comment: Comments, view: View?) {
        commentId = comment.commentId!!
        val intent = Intent(this, SelectTagActivity::class.java)
        intent.putExtra("tagList", tagsModel)
        intent.putExtra("commentId", comment.commentId!!)
        startActivityForResult(intent, 1)
    }

    override fun onRemoveTagComment(comment: Comments) {
        comment.tags?.let {
            commentId = comment.commentId!!
            commentDeleteTag(it[0], comment.commentId!!)
        }
    }

    override fun onDeleteComment(comment: Comments) {

    }

    fun getCadFileDetail(id: String) {
        runOnUiThread {
            MyUtils.showProgress(this, flProgress)
            APIOpenAirUtils.getCadFileDetail((application as LidarApp).prefManager!!.getToken(), id, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    val data = result as CadFileDetailResponse
                    data.response?.let {
                        cadFile = it
                        //not load model again
                        if (!isReply) {
                            reconstructions = it.reconstructions
                            it.tags?.let {
                                tagsModel?.addAll(it)
                                tagsAdapter?.notifyDataSetChanged()
//                                grvTags.scrollToPosition(it.size-1)
//                                showHideTag()
                            }

                            reconstructions?.let {
                                if (!isLoadHtml) {
//                                    it.forEachIndexed { index, element ->
//                                        //check scene new
//                                        if (element.scanningTypeID != 1 &&
//                                            !element.filePath.toString().contains(".glb")) {
//                                            getReconstructionHtml(cadFileID)
//                                            isLoadHtml = true
//                                            sceneView.visibility = View.GONE
//                                            webView.visibility = View.VISIBLE
//                                            return@forEachIndexed
//                                        }
//                                    }
                                }

                                if (!isLoadHtml) {
                                    for (model in it) {
                                        runOnUiThread {
                                            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/"+model.reconstructionID+".gltf")
                                            if (model.cadfilemodel?.position == null) {
                                                model.cadfilemodel?.position?.x = "0"
                                                model.cadfilemodel?.position?.y = "0"
                                                model.cadfilemodel?.position?.z = "0"
                                            }
                                            if (model.cadfilemodel?.scale == null) {
                                                model.cadfilemodel?.scale?.x = "1"
                                                model.cadfilemodel?.scale?.y = "1"
                                                model.cadfilemodel?.scale?.z = "1"
                                            }

                                            if (!file.exists()) {
                                                //check free storage
                                                var fileSize = 0
                                                model.fileSize?.let {
                                                    fileSize = round(it.toDouble()/(1024*1024)).toInt()
                                                }
                                                if (!MyUtils.getFreeSize(fileSize)) {
                                                    MyUtils.deleteFileGltf()
                                                }
                                                downloadModel(BuildConfig.URL_OA3D+ "reconstruction/model/"+model.reconstructionID, model)
                                            } else {
                                                index++
                                                loadModelUrl(this@BoardFileManagerDetailActivity, BuildConfig.URL_OA3D+ "reconstruction/model/"+model.reconstructionID, sceneView,
                                                    model.reconstructionID.toString(), model.modelName.toString())
                                                if (index == reconstructions?.size) {
                                                    index = 0
                                                    MyUtils.hideProgress(this@BoardFileManagerDetailActivity, flProgress)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        //add comment
                        it.comments?.let {
                            commentModel?.clear()
                            commentModel?.addAll(it)
                            commentAdapter?.notifyDataSetChanged()
                            grvComment.scrollToPosition(it.size-1)
                        }

                        //check comment size
                        if (commentModel!!.isNotEmpty()) {
                            grvComment.visibility = View.VISIBLE
                            tvEmptyComment.visibility = View.GONE
                        } else {
                            grvComment.visibility = View.GONE
                            tvEmptyComment.visibility = View.VISIBLE
                        }

                        //if back from reply ui
                        if (isReply) {
                            MyUtils.hideProgress(this@BoardFileManagerDetailActivity, flProgress)
                        }
                        isReply = false
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@BoardFileManagerDetailActivity, flProgress)
                    MyUtils.toastError(this@BoardFileManagerDetailActivity, error as ErrorModel)
                }
            })
        }
    }

    fun showHideTag() {
//        if (tagsModel!!.isNotEmpty()) {
//            grvTags.visibility = View.VISIBLE
//            tvEmptyTags.visibility = View.GONE
//        } else {
//            grvTags.visibility = View.GONE
//            tvEmptyTags.visibility = View.VISIBLE
//        }
    }

    fun addComment(id: String) {
        try {
            val gson = Gson()
            val comment = CommentRequest()
            comment.cadFileId = id
            comment.text = edtChatMessage.text.trim().toString()
            val obj = JSONObject(gson.toJson(comment))
            socketClient?.emit("post_comment", obj)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun deleteComment(id: String) {
        try {
            val gson = Gson()
            val request = DeleteCommentRequest()
            request.commentId = id
            val obj = JSONObject(gson.toJson(request))
            socketClient?.emit("delete_comment", obj)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun loadModel(html: String?) {
        MyUtils.loadModel(this, webView, "", html)
    }

    fun getReconstructionHtml(id: String) {
        APIOpenAirUtils.getReconstructionHTML((application as LidarApp).prefManager!!.getToken(),
            id, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@BoardFileManagerDetailActivity, flProgress)
                    val data = result as ResponseBody
                    if(data != null) {
                        htmlFile = data.string()
                        loadModel(htmlFile)
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@BoardFileManagerDetailActivity, flProgress)
                    MyUtils.toastError(this@BoardFileManagerDetailActivity, error as ErrorModel)
                }
            })
    }

    fun addTagModel(tagName: String, baseModel: String, cadFileId: String, position: PositionRequest) {
        val tagRequest = AddTagRequest()
        tagRequest.baseModel = baseModel
        tagRequest.name = tagName
        tagRequest.position = position
        tagRequest.cadFileId = cadFileId
        val gson = Gson()
        val obj = JSONObject(gson.toJson(tagRequest))
        socketClient?.emit("add_tags", obj)
    }

    fun editTagName(id: String, tagName: String, position: Int) {
        val tagRequest = TagRequest()
        tagRequest.name = tagName
        APIOpenAirUtils.editTagName((application as LidarApp).prefManager!!.getToken(),
            id, tagRequest, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@BoardFileManagerDetailActivity, flProgress)
                    tagsModel?.get(position)?.name = tagName
                    tagsModel?.get(position)?.isEdit = false
                    tagsAdapter?.notifyDataSetChanged()
                    var tagNode: TagNode? = null
                    for (tag in listTag!!) {
                        if (tag.tagId == id) {
                            tagNode = tag
                            sceneView.removeChild(tag.node)
                        }
                    }
                    tagNode?.let {
                        listTag?.remove(tagNode)
                    }

                    val viewDistance = LoadingNode(position = Position(x = tagsModel?.get(position)?.position?.x!!.toFloat(),
                        y = tagsModel?.get(position)?.position?.y!!.toFloat(), z = tagsModel?.get(position)?.position?.z!!.toFloat()+0.14f),
                        lifecycle = lifecycle, layoutResId = R.layout.text_node, context = this@BoardFileManagerDetailActivity,
                        textNode = tagsModel?.get(position)?.name)
                    modelNode?.addChild(viewDistance)
                    listTag?.add(TagNode(tagsModel?.get(position)?.tagId, viewDistance))
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@BoardFileManagerDetailActivity, flProgress)
                    MyUtils.toastError(this@BoardFileManagerDetailActivity, error as ErrorModel)
                }
            })
    }

    fun deleteTag(id: String, position: Int) {
        APIOpenAirUtils.deleteTag((application as LidarApp).prefManager!!.getToken(),
            id, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@BoardFileManagerDetailActivity, flProgress)
                    val data = result as BaseResponse
                    tagsModel?.removeAt(position)
                    tagsAdapter?.notifyDataSetChanged()
                    var tagNode: TagNode? = null
                    for (tag in listTag!!) {
                        if (tag.tagId == id) {
                            tagNode = tag
                            sceneView.removeChild(tag.node)
                        }
                    }
                    tagNode?.let {
                        listTag?.remove(tagNode)
                    }
//                    showHideTag()
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@BoardFileManagerDetailActivity, flProgress)
                    MyUtils.toastError(this@BoardFileManagerDetailActivity, error as ErrorModel)
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val tag = data?.getSerializableExtra("tag")
            commentAddTag(tag as Tags, commentId)
        }
    }

    fun commentAddTag(tag: Tags, commentId: String) {
        APIOpenAirUtils.commentAddTag((application as LidarApp).prefManager!!.getToken(),
            tag.tagId!!, commentId, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@BoardFileManagerDetailActivity, flProgress)
                    val data = result as CommentTagResponse
                    data.response?.let {
                        commentModel?.first { it.commentId == this@BoardFileManagerDetailActivity.commentId }?.tags?.add(tag)
                    }
                    popupWindow?.let {
                        this@BoardFileManagerDetailActivity.commentId = ""
                        popupWindow?.dismiss()
                        popupWindow = null
                    }
                    commentAdapter?.notifyDataSetChanged()
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@BoardFileManagerDetailActivity, flProgress)
                    MyUtils.toastError(this@BoardFileManagerDetailActivity, error as ErrorModel)
                }
            })
    }

    fun commentDeleteTag(tag: Tags, commentId: String) {
        APIOpenAirUtils.commentDeleteTag((application as LidarApp).prefManager!!.getToken(),
            tag.tagId!!, commentId, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@BoardFileManagerDetailActivity, flProgress)
                    commentModel?.first { it.commentId == this@BoardFileManagerDetailActivity.commentId }?.tags?.clear()
                    commentAdapter?.notifyDataSetChanged()
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@BoardFileManagerDetailActivity, flProgress)
                    MyUtils.toastError(this@BoardFileManagerDetailActivity, error as ErrorModel)
                }
            })
    }

    @SuppressLint("Range")
    fun downloadModel(dat: String, reconstruction: Rows) {
        val downloadManager: DownloadManager.Request = DownloadManager.Request(Uri.parse(dat))
        val title = URLUtil.guessFileName(dat, null, "model/gltf+json").replace(".bin", ".gltf")
        downloadManager.setTitle(title)
        downloadManager.setDescription("Downloading file please wait...")
//        downloadManager.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        downloadManager.setVisibleInDownloadsUi(false)
        downloadManager.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title)
        val download: DownloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        var downloadId = download.enqueue(downloadManager)
        Thread {
            var downloading = true
            while (downloading) {
                val q = DownloadManager.Query()
                q.setFilterById(downloadId)
                val cursor: Cursor = download.query(q)
                cursor.moveToFirst()
                val bytes_downloaded = cursor.getInt(
                    cursor
                        .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                )
                val bytes_total =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val dl_progress = (bytes_downloaded * 100L / bytes_total).toInt()
                runOnUiThread {
                    if (dl_progress == 100) {
                        index++
                        val handler = Handler()
                        handler.postDelayed(Runnable {
                            // Actions to do after 1 seconds
                            loadModelUrl(this@BoardFileManagerDetailActivity, BuildConfig.URL_OA3D+ "reconstruction/model/"+reconstruction.reconstructionID, sceneView,
                                reconstruction.reconstructionID.toString(), reconstruction.modelName.toString())
                        }, 1000)
                        if (index == reconstructions?.size) {
                            index = 0
                            MyUtils.hideProgress(this@BoardFileManagerDetailActivity, flProgress)
                        }
                    }
                }
                cursor.close()
            }
        }.start()
    }

    fun loadModelUrl(activity: Activity, url: String?, sceneView: io.github.sceneview.SceneView, reconstructionId: String, name: String) {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/"+reconstructionId+".gltf")
//        ModelRenderable.builder()
//            .setSource(
//                activity,
//                Uri.parse(file.toString())
//            )
//            .setIsFilamentGltf(true)
//            .build()
//            .thenAccept { modelRenderable ->
////                addNodeToScene(modelRenderable, Vector3(position.x!!.toFloat(), position.y!!.toFloat(), position.z!!.toFloat()),
////                    Vector3(scale.x!!.toFloat(), scale.y!!.toFloat(), scale.z!!.toFloat()), name)
//                    }
//
//            .exceptionally { throwable ->
//                val toast = Toast.makeText(
//                    activity,
//                    "Unable to load model ".plus(name),
//                    Toast.LENGTH_LONG
//                )
//                toast.setGravity(Gravity.CENTER, 0, 0)
//                toast.show()
//                null
//            }
        modelNode = ModelNode(
            position = Position(z = 0.0f),
            rotation = Rotation(x = 0.0f),
        )
//        modelNode.parent = sceneView
//        sceneView.cameraNode.transform = lookAt(
//            eye = modelNode.worldPosition.let {
//                Position(x = it.x, y = it.y + 0.5f, z = it.z + 2.0f)
//            },
//            target = modelNode.worldPosition,
//            up = Direction(y = 1.0f)
//        )
        modelNode?.let {
            sceneView.addChild(it)
            lifecycleScope.launchWhenCreated {
                it.loadModel(
                    context = activity,
                    lifecycle = lifecycle,
                    glbFileLocation = file.path,
                    autoAnimate = false,
                    scaleToUnits = 1.0f,
                    centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0f)
                )
                delay(500)
            }
            for (tag in tagsModel!!) {
                tag.position?.x?.let {
                    runOnUiThread {
                        val viewDistance = LoadingNode(position = Position(x = tag.position?.x!!.toFloat(),
                            y = tag.position?.y!!.toFloat(), z = tag.position?.z!!.toFloat()+0.14f),
                            lifecycle = lifecycle, layoutResId = R.layout.text_node, context = this@BoardFileManagerDetailActivity,
                            textNode = tag.name)
                        modelNode?.addChild(viewDistance)
                        listTag?.add(TagNode(tag.tagId, viewDistance))
                    }
                }
            }
        }

//        sceneView.cameraNode.smooth(
//            lookAt(
//                eye = modelNode.worldPosition.let {
//                    Position(x = it.x - 0.4f, y = it.y + 0.4f, z = it.z - 0.6f)
//                },
//                target = modelNode.worldPosition,
//                up = Direction(y = 1.0f)
//            ),
//            speed = 0.7f
//        )
    }

    override fun onListener(tag: Tags) {
        commentAddTag(tag, commentId)
    }

    //edit tag
    override fun onEdit(position: Int, tagId: String, tagName: String) {
        tagsModel?.get(position)?.isEdit = false
        editTagName(tagId, tagName, position)
    }

    //delete tag
    override fun onDelete(position: Int, tagId: String, tagName: String) {
        DialogFactory.dialogConfirmDelete(
            this,
            object : DialogFactory.Companion.DialogListener.Delete {
                override fun delete() {
                    deleteTag(tagId, position)
                }
            }, " ".plus(tagName))
    }
}