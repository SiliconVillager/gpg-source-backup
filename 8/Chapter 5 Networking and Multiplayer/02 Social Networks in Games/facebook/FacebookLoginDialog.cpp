//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#include "facebooklogindialog.h"
#include "ui_facebooklogindialog.h"

#include <QWebFrame>

//-----------------------------------------------------------------------------
namespace
{
  const char FACEBOOK_RESULT_BASE[] = "http://www.facebook.com/connect/";
  const char FACEBOOK_RESULT_SUCCESS[] = "login_success.html";
  const char FACEBOOK_RESULT_FAILURE[] = "login_failure.html";
}

//-----------------------------------------------------------------------------
FacebookLoginDialog::FacebookLoginDialog(QString apiKey, QWidget *parent) :
    QDialog(parent),
    m_Ui(new Ui::FacebookLoginDialog),
    m_SessionKey(),
    m_Uid(),
    m_SessionSecret(),
    m_LoadInProgress(false)
{
  m_Ui->setupUi(this);
  connect(m_Ui->webView, SIGNAL(loadStarted()), this, SLOT(loadStarted()));
  connect(m_Ui->webView, SIGNAL(loadFinished(bool)), this, SLOT(loadFinished(bool)));
  connect(m_Ui->webView, SIGNAL(urlChanged(const QUrl&)), this, SLOT(urlChanged(const QUrl&)));
  connect(m_Ui->closeButton, SIGNAL(clicked()), this, SLOT(reject()));

  QString loginUrl;
  loginUrl.sprintf("http://www.facebook.com/login.php?"
          "api_key=%s&connect_display=popup&v=1.0&next=%s%s&cancel_url=%s%s&"
          "fbconnect=true&return_session=true&req_perms=offline_access,publish_stream",
          apiKey.toAscii().constData(),
          FACEBOOK_RESULT_BASE, FACEBOOK_RESULT_SUCCESS,
          FACEBOOK_RESULT_BASE, FACEBOOK_RESULT_FAILURE);
  m_Ui->webView->load(loginUrl);
  qDebug("-> Loading: '%s'", loginUrl.toAscii().constData());
}

//-----------------------------------------------------------------------------
FacebookLoginDialog::~FacebookLoginDialog()
{
    delete m_Ui;
}

//-----------------------------------------------------------------------------
void FacebookLoginDialog::loadStarted()
{
  m_LoadInProgress = true;
  setWindowTitle("Facebook Login - Loading...");
}

//-----------------------------------------------------------------------------
void FacebookLoginDialog::loadFinished(bool ok)
{
//  if (m_LoadInProgress)
  {
    m_LoadInProgress = false;
    if (ok)
    {
      setWindowTitle("Facebook Login - Loading...finished.");
    }
    else
    {
      setWindowTitle("Facebook Login - Loading...error.");
      QString page = m_Ui->webView->page()->mainFrame()->toHtml();
      QString url = m_Ui->webView->url().toString();
      qDebug("<- Error: '%s'\n%s", url.toAscii().constData(),
             page.toAscii().constData());
    }
  }
}

//-----------------------------------------------------------------------------
void FacebookLoginDialog::urlChanged(const QUrl& url)
{
  QString path = url.path();
  if (path.endsWith(FACEBOOK_RESULT_SUCCESS))
  {
    // Parse JSON response with regular expressions.
    // "{"session_key":"1d8a49ee93b35c198fbee820-1417416340",
    //   "uid":"1417416340","expires":0,
    //   "secret":"0fb6e4c70cd9e510ad3b493fcc31bb74",
    //   "sig":"73a9a4be4b6d5d099f017f4d999b89c4"}"
    QString sessionAsJson = url.queryItemValue("session");
    QRegExp findSessionKey("\"session_key\"\\s*:\\s*\"([^\"]+)\"");
    findSessionKey.indexIn(sessionAsJson);
    m_SessionKey = findSessionKey.cap(1);
    QRegExp findUid("\"uid\"\\s*:\\s*\"([^\"]+)\"");
    findUid.indexIn(sessionAsJson);
    m_Uid = findUid.cap(1);
    QRegExp findSessionSecret("\"secret\"\\s*:\\s*\"([^\"]+)\"");
    findSessionSecret.indexIn(sessionAsJson);
    m_SessionSecret = findSessionSecret.cap(1);

    bool success = !m_SessionKey.isEmpty() && !m_Uid.isEmpty() && !m_SessionSecret.isEmpty();
    if (success)
    {
      // Close dialog and return success.
      accept();
    }
    else
    {
      // Close dialog and return failure.
      reject();
    }
  }
  else if (path.endsWith(FACEBOOK_RESULT_FAILURE))
  {
    // Close dialog and return failure.
    reject();
  }
  else
  {
    setWindowTitle(url.path());
  }
}
