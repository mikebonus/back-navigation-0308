package com.luxpmsoft.luxaipoc.view.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.BuildConfig
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.CommentAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.comment.CommentRequest
import com.luxpmsoft.luxaipoc.model.comment.CommentResponse
import com.luxpmsoft.luxaipoc.model.recentmodel.CadFileDetailResponse
import com.luxpmsoft.luxaipoc.model.recentmodel.Comments
import com.luxpmsoft.luxaipoc.utils.MyUtils
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.Transport
import kotlinx.android.synthetic.main.activity_reply_chat.*
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.*
import javax.net.ssl.*
import kotlin.collections.ArrayList

class ReplyChatActivity: AppCompatActivity(), CommentAdapter.OnListener {
    var commentAdapter: CommentAdapter? = null
    var commentModel: ArrayList<Comments>? = ArrayList()
    var comment: Comments? = null
    var socketClient: Socket?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reply_chat)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        init()
        listener()
        try {
            initSocket()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun init() {
        Utils.gridLayoutManager(this, grvReplyChat, 1, GridLayoutManager.VERTICAL)
        commentAdapter = CommentAdapter(this, R.layout.item_comment, commentModel!!, this)
        grvReplyChat.adapter = commentAdapter
        val bundle = intent.extras
        bundle?.let {
            comment = Gson().fromJson(it.getString("comment").toString(), Comments::class.java)
            tvUserName.text = comment?.user?.full_name
            tvTime.text = comment?.createdAt?.let { it1 -> MyUtils.convertToLocalTimeHHmm(it1) }
            tvComment.text = comment?.text
            comment?.user?.profileImageKey?.let {
                MyUtils.loadAvatar(
                    this, (application as LidarApp).prefManager!!.getToken(),
                    it, imgAvatar, resources.getDrawable(R.drawable.user))
            }
            comment?.tags?.let {
                if (it.isNotEmpty()) {
                    tagName.visibility = View.VISIBLE
                    tagName.text = it[0].name
                }
            }
            getComment(comment?.commentId)

        }

        MyUtils.loadAvatar(
            this, (application as LidarApp).prefManager?.getToken(),
            (application as LidarApp).prefManager?.getProfileImageKey(), avatar,
            resources.getDrawable(R.drawable.user))
    }

    fun listener() {
        lineSend.setOnClickListener {
            if (edtChatMessage.text.toString().trim().isNotEmpty()) {
                comment?.commentId?.let { it1 -> addComment(it1) }
            }
        }
        icBack.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        commentModel?.let {
            commentModel = null
        }

        comment?.let {
            comment = null
        }

        socketClient?.disconnect()
        socketClient?.off("get_comment")
        socketClient?.off("cadfile_updates")
    }

    override fun onListener(comment: Comments) {
    }

    override fun onAddTagComment(comment: Comments, view: View?) {

    }

    override fun onRemoveTagComment(comment: Comments) {

    }

    override fun onDeleteComment(comment: Comments) {

    }

    fun addComment(parentId: String) {
        try {
            val gson = Gson()
            val commentRequest = CommentRequest()
            commentRequest.parentCommentId = parentId
            commentRequest.text = edtChatMessage.text.trim().toString()
            val obj = JSONObject(gson.toJson(commentRequest))
            socketClient?.emit("post_comment", obj)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun getComment(commentId: String?) {
        MyUtils.showProgress(this, flProgress)
        APIOpenAirUtils.getComment((application as LidarApp).prefManager!!.getToken(), commentId, object : APIInterface.onDelegate {
            override fun onSuccess(result: Any?) {
                MyUtils.hideProgress(this@ReplyChatActivity, flProgress)
                val comment = result as CommentResponse
                comment.response?.let {
                    it?.comments?.let {
                        commentModel?.clear()
                        commentModel?.addAll(it)
                        commentAdapter?.notifyDataSetChanged()
                    }
                    commentModel?.let {
                        tvReply.text = it.size.toString().plus(" replies")
                    }
                }
            }

            override fun onError(error: Any?) {
                MyUtils.hideProgress(this@ReplyChatActivity, flProgress)
                MyUtils.toastError(this@ReplyChatActivity, error as ErrorModel)
            }
        })
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
            socketClient?.on(
                Socket.EVENT_CONNECT,
                Emitter.Listener {
                    Log.e("EVENT_CONNECT", "socket connected")
                })?.on(
                Socket.EVENT_DISCONNECT,
                Emitter.Listener {
                    Log.e("EVENT_DISCONNECT", "socket disconnected")
                })?.on(
                Socket.EVENT_CONNECT_ERROR,
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

    private fun cadfileUpdates() {
        socketClient?.on("cadfile_updates", Emitter.Listener { args ->
            try {
                val messageJson = JSONObject(args[0].toString())
                val obj: CadFileDetailResponse = Gson().fromJson(messageJson.toString(), CadFileDetailResponse::class.java)
                runOnUiThread {
                    obj.response?.let {
                        it.comments?.let {
                            val model = it.first { it.commentId == comment?.commentId }
                            model?.comments?.let {
                                commentModel?.clear()
                                commentModel?.addAll(it)
                                commentAdapter?.notifyDataSetChanged()
                                grvReplyChat.scrollToPosition(it.size-1)
                            }
                        }
                    }
                    edtChatMessage.setText("")
                    commentModel?.let {
                        tvReply.text = it.size.toString().plus(" replies")
                    }
                    MyUtils.hideKeyboard(this@ReplyChatActivity)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        })
    }

    private fun getComment() {
        socketClient?.on("get_comment", Emitter.Listener { args ->
        })
    }
}