//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#include "mainwindow.h"
#include "ui_mainwindow.h"

#include <QSettings>
#include <QNetworkAccessManager>
#include <QUrl>
#include <QNetworkRequest>
#include <QDesktopServices>

#include "facebook/FacebookLoginDialog.h"
#include "facebook/FacebookService.h"

//-----------------------------------------------------------------------------
namespace
{
  #error "Copy Facebook API key & secret here (see http://developers.facebook.com/get_started.php)"
  const char FACEBOOK_API_KEY[]     = "";
  const char FACEBOOK_APP_SECRET[]  = "";
}

//-----------------------------------------------------------------------------
MainWindow::MainWindow(QWidget *parent)
  : QMainWindow(parent)
  , m_Ui(new Ui::MainWindow)
  , m_LoginState(LS_LoggedOut)
{
  // Initialize UI.
  m_Ui->setupUi(this);
  connect(m_Ui->loginButton, SIGNAL(clicked()), this, SLOT(loginButtonClicked()));
  connect(m_Ui->refreshFriendsButton, SIGNAL(clicked()), this, SLOT(refreshFriendsButtonClicked()));
  connect(m_Ui->refreshAppUsersButton, SIGNAL(clicked()), this, SLOT(refreshAppUsersButtonClicked()));
  connect(m_Ui->sendMessageButton, SIGNAL(clicked()), this, SLOT(sendMessageButtonClicked()));
  m_Ui->statusBar->showMessage("Welcome");

  // Set default values for persistent user settings (QSettings).
  QCoreApplication::setOrganizationName("Gems8");
  QCoreApplication::setApplicationName("Facebook");

  // Check if there's a session from last time.
  m_FacebookService = new FacebookService(FACEBOOK_API_KEY);
  QString key, secret, storedUid;
  bool hasSession = hasFacebookSession(key, secret, storedUid);

  // Comment in to use a debugging proxy.
//  m_FacebookService->setProxy("localhost", 8888);

  if (hasSession)
  {
    // Check with Facebook if the session is still valid.
    m_FacebookService->setSession(secret, key);
    FacebookUid uid;
    m_FacebookService->getLoggedInUser(uid);
    m_Ui->statusBar->showMessage(uid);
    if (uid != storedUid)
    {
      // User IDs don't match -> session can't be used.
      hasSession = false;
      m_FacebookService->setSession("", "");
      storeFacebookSession("", "", "");
    }
  }

  setLoginState(hasSession ? LS_LoggedIn : LS_LoggedOut);
}

//-----------------------------------------------------------------------------
MainWindow::~MainWindow()
{
  delete m_Ui;
  delete m_FacebookService;
}

//-----------------------------------------------------------------------------
void MainWindow::loginButtonClicked()
{
  switch (loginState())
  {
    case LS_LoggedIn:
    {
      // Delete persistent session if user logs out.
      setLoginState(LS_LoggedOut);
      storeFacebookSession("", "", "");
    }
    break;

    case LS_LoggedOut:
    {
      if (m_Ui->integratedBox->isChecked())
      {
        // Login with integrated browser (WebKit).
        FacebookLoginDialog* dlg = new FacebookLoginDialog(FACEBOOK_API_KEY,
                                                           m_Ui->centralWidget);
        if (dlg->exec() == QDialog::Accepted)
        {
          // Success: persist session.
          setLoginState(LS_LoggedIn);
          storeFacebookSession(dlg->sessionKey(), dlg->sessionSecret(), dlg->uid());
        }
        else
        {
          // Failure or cancel: user can try again.
          setLoginState(LS_LoggedOut);
        }
        delete dlg;
      }
      else
      {
        // Login with external browser (first step: create token).
        m_FacebookService->setSession(FACEBOOK_APP_SECRET, "");
        startRequest();
        FacebookServiceState result = m_FacebookService->createToken();
        endRequest(result);

        if (m_FacebookService->hasToken())
        {
          // Load login page in external browser.
          startRequest();
          QString loginUrl;
          m_FacebookService->getLoginUrl(loginUrl);
          qDebug("-> Loading: '%s'", loginUrl.toAscii().constData());
          bool result = QDesktopServices::openUrl(loginUrl);
          qDebug("<- %s", result ? "Success" : "Error");
          endRequest(result ? FacebookServiceStates::Ok : FacebookServiceStates::NetworkError);
          setLoginState(LS_LoginInProgress);
        }
        else
        {
          // Error
          setLoginState(LS_LoggedOut);
        }
      }
    }
    break;

    case LS_LoginInProgress:
    {
      // Login with external browser (second step: get session).
      FacebookGetSessionResult sessionResult;
      startRequest();
      FacebookServiceState result = m_FacebookService->getSession(sessionResult);
      endRequest(result);
      if (!sessionResult.sessionKey.isEmpty() &&
          !sessionResult.sessionSecret.isEmpty() &&
          !sessionResult.uid.isEmpty())
      {
        // Success: persist session.
        storeFacebookSession(sessionResult.sessionKey, sessionResult.sessionSecret,
          sessionResult.uid);

        // Load permission page in external browser.
        startRequest();
        QString permissionUrl;
        m_FacebookService->getPermissionUrl(permissionUrl);
        qDebug("-> Loading: '%s'", permissionUrl.toAscii().constData());
        bool result = QDesktopServices::openUrl(permissionUrl);
        qDebug("<- %s", result ? "Success" : "Error");
        endRequest(result ? FacebookServiceStates::Ok : FacebookServiceStates::NetworkError);
        setLoginState(LS_PermissionInProgress);
      }
      else
      {
        // Failure or cancel: user can try again.
        setLoginState(LS_LoggedOut);
      }
    }
    break;

    case LS_PermissionInProgress:
    {
      // Login with external browser (third step: confirm session).
      setLoginState(LS_LoggedIn);
    }
    break;
  }
}

//-----------------------------------------------------------------------------
void MainWindow::refreshFriendsButtonClicked()
{
  startRequest();

  // Update session.
  QString key, secret, uid;
  bool hasSession = hasFacebookSession(key, secret, uid);
  assert(hasSession);
  m_FacebookService->setSession(secret, key);

  // Get all the user's friends.
  FacebookFriends friends;
  FacebookServiceState result = m_FacebookService->getFriends(friends);
  m_Ui->friendsEdit->setText(QString("%1").arg(friends.size()));

  endRequest(result);
}

//-----------------------------------------------------------------------------
void MainWindow::refreshAppUsersButtonClicked()
{
  startRequest();

  // Update session.
  QString key, secret, uid;
  bool hasSession = hasFacebookSession(key, secret, uid);
  assert(hasSession);
  m_FacebookService->setSession(secret, key);

  // Get only friends that use this application.
  FacebookAppUsers appUsers;
  FacebookServiceState result = m_FacebookService->getAppUsers(appUsers);
  m_Ui->appUsersEdit->setText(QString("%1").arg(appUsers.size()));

  endRequest(result);
}

//-----------------------------------------------------------------------------
void MainWindow::sendMessageButtonClicked()
{
  startRequest();

  // Update session.
  QString key, secret, uid;
  bool hasSession = hasFacebookSession(key, secret, uid);
  assert(hasSession);
  m_FacebookService->setSession(secret, key);

  // Get only friends that use this application.
  FacebookServiceState result = m_FacebookService->sendMessage(
      m_Ui->messageEdit->text());

  endRequest(result);
}

//-----------------------------------------------------------------------------
void MainWindow::setLoginState(LoginState state)
{
  switch (state)
  {
    case LS_LoggedIn:
    {
      m_Ui->refreshFriendsButton->setEnabled(true);
      m_Ui->refreshAppUsersButton->setEnabled(true);
      m_Ui->sendMessageButton->setEnabled(true);
      m_Ui->loginButton->setText("Disconnect");
      m_Ui->statusBar->showMessage("Logged in");
    }
    break;

    case LS_LoggedOut:
    {
      m_Ui->refreshFriendsButton->setEnabled(false);
      m_Ui->refreshAppUsersButton->setEnabled(false);
      m_Ui->sendMessageButton->setEnabled(false);
      m_Ui->loginButton->setText("Login");
//      m_Ui->statusBar->showMessage("Not logged in");
    }
    break;

    case LS_LoginInProgress:
    {
      m_Ui->refreshFriendsButton->setEnabled(false);
      m_Ui->refreshAppUsersButton->setEnabled(false);
      m_Ui->sendMessageButton->setEnabled(false);
      m_Ui->loginButton->setText("Confirm session");
      m_Ui->statusBar->showMessage("Waiting for login");
    }
    break;

    case LS_PermissionInProgress:
    {
      m_Ui->refreshFriendsButton->setEnabled(false);
      m_Ui->refreshAppUsersButton->setEnabled(false);
      m_Ui->sendMessageButton->setEnabled(false);
      m_Ui->loginButton->setText("Confirm permissions");
      m_Ui->statusBar->showMessage("Waiting for permission");
    }
    break;
  }
  m_LoginState = state;
}

//-----------------------------------------------------------------------------
MainWindow::LoginState MainWindow::loginState() const
{
  return m_LoginState;
}

//-----------------------------------------------------------------------------
void MainWindow::startRequest()
{
  m_Ui->loginButton->setEnabled(false);
  m_Ui->refreshFriendsButton->setEnabled(false);
  m_Ui->refreshAppUsersButton->setEnabled(false);
  m_Ui->sendMessageButton->setEnabled(false);
  m_Ui->progressBar->setMinimum(0);
  m_Ui->progressBar->setMaximum(0);
}

//-----------------------------------------------------------------------------
void MainWindow::endRequest(FacebookServiceState state)
{
  QString message;
  switch (state)
  {
    case FacebookServiceStates::NetworkError: message = "Network error"; break;
    case FacebookServiceStates::PermissionsError: message = "Permissions error"; break;
    case FacebookServiceStates::SessionError: message = "Session error"; break;
    case FacebookServiceStates::XmlError: message = "XML error"; break;
    case FacebookServiceStates::Ok: message = "Success"; break;
    default: message = "Error";
  }
  m_Ui->statusBar->showMessage(message);
  m_Ui->loginButton->setEnabled(true);
  m_Ui->refreshFriendsButton->setEnabled(true);
  m_Ui->refreshAppUsersButton->setEnabled(true);
  m_Ui->sendMessageButton->setEnabled(true);
  m_Ui->progressBar->setMinimum(100);
  m_Ui->progressBar->setMaximum(100);
}

//-----------------------------------------------------------------------------
void MainWindow::storeFacebookSession(QString sessionKey, QString sessionSecret,
  QString uid) const
{
  QSettings settings;
  settings.setValue("sessionKey", sessionKey);
  settings.setValue("sessionSecret", sessionSecret);
  settings.setValue("uid", uid);
}

//-----------------------------------------------------------------------------
bool MainWindow::hasFacebookSession(QString& sessionKey, QString& sessionSecret,
  QString& uid) const
{
  QSettings settings;
  sessionKey = settings.value("sessionKey").toString();
  sessionSecret = settings.value("sessionSecret").toString();
  uid = settings.value("uid").toString();

  return (!sessionKey.isEmpty() && !uid.isEmpty() && !sessionSecret.isEmpty());
}
