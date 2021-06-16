//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#ifndef WEB_FACEBOOK_FACEBOOKSERVICE_H
#define WEB_FACEBOOK_FACEBOOKSERVICE_H

#include "FacebookCreateToken.h"
#include "FacebookGetLoggedInUser.h"
#include "FacebookGetFriends.h"
#include "FacebookGetAppUsers.h"
#include "FacebookSendMessage.h"
#include "FacebookGetSession.h"
#include "FacebookServiceStates.h"
#include "FacebookServiceUtils.h"

#include <QDomDocument>
#include <QEventLoop>
#include <QNetworkAccessManager>
#include <QNetworkProxy>
#include <QNetworkReply>
#include <QNetworkRequest>
#include <QUrl>
#include <QXmlInputSource>

#include <cassert>

//==============================================================================
// Declarations
//==============================================================================

/** C++ implementation of Facebook API. */
class FacebookService : public QObject
{
  Q_OBJECT

public:

  /**
   * Creates a new service instance.
   * @param apiKey key to identify application.
   */
  FacebookService(QString apiKey);

  /** Destructor. */
  ~FacebookService();

  /**
   * Manually sets the session identifiers.
   * @param secret secret to use for signing a request.
   * @param sessionKey session key.
   */
  inline void setSession(QString secret, QString sessionKey);

  /**
   * Changes proxy settings for network connections.
   * @param server proxy server; empty string to unset proxy.
   * @param port proxy port.
   * @param user optional user name for proxy authentication.
   * @param password optional password for proxy authentication.
   */
  inline void setProxy(QString server, int port = 80, QString user = QString(),
    QString password = QString());

  /**
   * Requests an authentication token to start authorizing a session.
   * @param appSecret Facebook application secret.
   */
  inline FacebookServiceState createToken();
  
  /**
   * Checks if there's a valid authentication token.
   * @return true if authentication token is valid; false otherwise.
   */
  inline bool hasToken() const;

  /**
   * URL of login page to authorize session.
   * @param URL.
   */
  inline void getLoginUrl(QString& url) const;

  /**
   * URL of page to request additional permission.
   * @param URL.
   */
  inline void getPermissionUrl(QString& url) const;

  /**
   * Authorizes a new session.
   * @param appSecret Facebook application secret.
   */
  inline FacebookServiceState getSession(FacebookGetSessionResult& sessionResult);

  /**
   * Checks if there's a valid session.
   * @return true if session is valid; false otherwise.
   */
  inline bool hasSession() const;

  /**
   * Returns a list of friends associated with the currently authenticated user.
   * @param friends friends list.
   * @return result code.
   */
  inline FacebookServiceState getFriends(FacebookFriends& friends);

  /**
   * Returns a list of friends associated with the currently authenticated user
   * that also have authorized this application.
   * @param friends friends list.
   * @return result code.
   */
  inline FacebookServiceState getAppUsers(FacebookAppUsers& appUsers);

  /**
   * Posts a message to the user's Facebook page.
   * @param message message.
   * @return result code.
   */
  inline FacebookServiceState sendMessage(FacebookMessage message);

  /**
   * Returns friends associated with the currently authenticated user.
   * @param friends friends list.
   * @return result code.
   */
  inline FacebookServiceState getLoggedInUser(FacebookUid& uid);

  /**
   * Generic service request.
   * @param requestOperator functor to create request.
   * @param input input data.
   * @param responseOperator functor to parse response.
   * @param output output data.
   * @return true if request was processed successfully; false otherwise.
   */
  template<class RequestOperator, class ResponseOperator> 
  FacebookServiceState service( 
    RequestOperator requestOperator, const typename RequestOperator::Input& input,
    ResponseOperator responseOperator, typename ResponseOperator::Output& output);

private slots:

  /**
   * Called when a request has finished.
   * @param reply network reply.
   */
  void requestFinished(QNetworkReply* reply);

private:

  QNetworkAccessManager m_Http;       ///< The HTTP client connection.
  QEventLoop m_EventLoop;             ///< Event loop to handle asynchronous network requests.
  QString m_ApiKey;                   ///< Key to identify application.
  QString m_Secret;                   ///< Secret to authenticate application.
  QString m_AuthenticationToken;      ///< Key to authorize session request.
  QString m_SessionKey;               ///< Key to authorize a session.
};

//==============================================================================
// Inline Methods
//==============================================================================

//------------------------------------------------------------------------------
inline void FacebookService::setSession(QString sessionSecret, QString sessionKey)
{
  m_Secret = sessionSecret;
  m_SessionKey = sessionKey;
}

//------------------------------------------------------------------------------
inline void FacebookService::setProxy(QString server, int port,
  QString user, QString password)
{
  QNetworkProxy proxy;
  proxy.setType(QNetworkProxy::HttpProxy);
  proxy.setHostName(server);
  proxy.setPort(port);
  proxy.setUser(user);
  proxy.setPassword(password);
  m_Http.setProxy(proxy);
}

//------------------------------------------------------------------------------
inline FacebookServiceState FacebookService::createToken()
{
  m_SessionKey.clear();   // session needs to get renewed
  return service(FacebookCreateTokenRequest(), NULL,
    FacebookCreateTokenResponse(), m_AuthenticationToken);
}

//------------------------------------------------------------------------------
inline bool FacebookService::hasToken() const
{
  return !m_AuthenticationToken.isEmpty();
}

//------------------------------------------------------------------------------
inline void FacebookService::getLoginUrl(QString& url) const
{
  assert(hasToken() && "Missing authentication token.");
  url = "http://www.facebook.com/login.php?v=1.0&api_key=%1&auth_token=%2";
  url = url.arg(m_ApiKey.toAscii().constData(), m_AuthenticationToken.toAscii().constData());
}

//------------------------------------------------------------------------------
inline void FacebookService::getPermissionUrl(QString& url) const
{
  url = "http://www.facebook.com/connect/prompt_permissions.php?api_key=%1&v=1.0&ext_perm=publish_stream";
  url = url.arg(m_ApiKey.toAscii().constData());
}

//------------------------------------------------------------------------------
inline FacebookServiceState FacebookService::getSession(FacebookGetSessionResult& sessionResult)
{
  assert(hasToken() && "Missing authentication token.");

  FacebookServiceState state = service(FacebookGetSessionRequest(), m_AuthenticationToken,
    FacebookGetSessionResponse(), sessionResult);

  return state;
}

//------------------------------------------------------------------------------
inline bool FacebookService::hasSession() const
{
  return !m_SessionKey.isEmpty();
}

//------------------------------------------------------------------------------
inline FacebookServiceState FacebookService::getFriends(FacebookFriends& friends)
{
  //assert(hasToken() && "Missing authentication token.");
  assert(hasSession() && "Missing session key.");

  return service(FacebookGetFriendsRequest(), NULL,
    FacebookGetFriendsResponse(), friends);
}

//------------------------------------------------------------------------------
inline FacebookServiceState FacebookService::getAppUsers(FacebookAppUsers& appUsers)
{
  //assert(hasToken() && "Missing authentication token.");
  assert(hasSession() && "Missing session key.");

  return service(FacebookGetAppUsersRequest(), NULL,
    FacebookGetAppUsersResponse(), appUsers);
}

//------------------------------------------------------------------------------
inline FacebookServiceState FacebookService::sendMessage(FacebookMessage message)
{
  //assert(hasToken() && "Missing authentication token.");
  assert(hasSession() && "Missing session key.");

  FacebookSendMessageResponse::Output output;
  return service(FacebookSendMessageRequest(), message,
    FacebookSendMessageResponse(), output);
}

//------------------------------------------------------------------------------
inline FacebookServiceState FacebookService::getLoggedInUser(FacebookUid& uid)
{
  //assert(hasToken() && "Missing authentication token.");
  assert(hasSession() && "Missing session key.");

  return service(FacebookGetLoggedInUserRequest(), NULL,
    FacebookGetLoggedInUserResponse(), uid);
}

//------------------------------------------------------------------------------
template<class RequestOperator, class ResponseOperator> 
FacebookServiceState FacebookService::service( 
  RequestOperator requestOperator, const typename RequestOperator::Input& input,
  ResponseOperator responseOperator, typename ResponseOperator::Output& output)
{
  const bool apiKeyValid = !m_ApiKey.isEmpty();
  const bool authenticationValid = 
    !RequestOperator::requiresSession || !m_SessionKey.isEmpty();
  
  assert(authenticationValid && "Authentication required for this service.");
  assert(apiKeyValid && "API key required.");

  FacebookServiceState result = FacebookServiceStates::Ok;
  if (authenticationValid && apiKeyValid)
  {
    // Request-specific data.
    QVector<QString> parameters;
    QString method = RequestOperator::requestName;
    result = requestOperator(input, parameters);
    int callId = FacebookServiceUtils::generateCallId();

    // Common request parameters.
    parameters.append(QString("api_key=%1").arg(m_ApiKey));
    parameters.append(QString("call_id=%1").arg(callId));
    parameters.append(QString("session_key=%1").arg(m_SessionKey));
    parameters.append(QString("method=%1").arg(method));
    parameters.append(QString("v=1.0"));

    // Sign request.
    QString signature = FacebookServiceUtils::sign(parameters, m_Secret);

    // Assemble POST data.
    QByteArray requestData;
    FacebookServiceUtils::createPostData(parameters, signature, requestData);

    // Execute POST request.
    qDebug("-> Sending: %s\n%s", method.toAscii().constData(), requestData.data());
    QNetworkReply* reply = m_Http.post(QNetworkRequest(
        QUrl("https://api.facebook.com/restserver.php")), requestData);

    if (reply != NULL)
    {
      // The HTTP request will signal FacebookService::requestFinished() and
      // then unlock the event loop. (This will still dispatch UI events.)
      m_EventLoop.exec();

      QByteArray replyData = reply->readAll();
      if (reply->error() > 0)
      {
        qDebug("<- Error:\n%s", reply->errorString().toAscii().constData());
      }
      else
      {
        qDebug("<- Received:\n%s", replyData.data());
      }

      QDomDocument document;
      result = FacebookServiceUtils::checkForError(reply, replyData, document);

      if (result == FacebookServiceStates::Ok)
      {
        // Parse result.
        result = responseOperator(document, output);
      }
    }
  }

  return result;
}

#endif // WEB_FACEBOOK_FACEBOOKSERVICE_H
