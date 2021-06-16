//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#ifndef WEB_FACEBOOK_FACEBOOKLOGINDIALOG_H
#define WEB_FACEBOOK_FACEBOOKLOGINDIALOG_H

#include <QtGui/QDialog>

//==============================================================================
// Forward Declarations
//==============================================================================

class QUrl;
namespace Ui {
    class FacebookLoginDialog;
}

//==============================================================================
// Declarations
//==============================================================================

/** Dialog for displaying the Facebook login page. */
class FacebookLoginDialog : public QDialog
{
  Q_OBJECT

public:

  /**
   * Creates a new dialog.
   * @param apiKey API key for the Facebook requests.
   * @param parent UI parent for the dialog.
   */
  FacebookLoginDialog(QString apiKey, QWidget *parent = 0);

  /** Destructor. */
  ~FacebookLoginDialog();

  //@{
  /** Getter/setter. */
  inline QString sessionKey() const;
  inline QString uid() const;
  inline QString sessionSecret() const;
  //@}

public slots:

  /** Called when the browser component starts loading a web page. */
  void loadStarted();

  /**
   * Called when the browser component finished loading a web page.
   * @param ok true if loading was successful; false otherwise.
   */
  void loadFinished(bool ok);

  /**
   * Called when the URL displayed in the browser component changes.
   * @param url URL that's being displayed.
   */
  void urlChanged(const QUrl& url);

private:

  Ui::FacebookLoginDialog *m_Ui;  ///< User interface.
  QString m_SessionKey;           ///< Contains the session key after a successful login.
  QString m_Uid;                  ///< Contains the user ID secret after a successful login.
  QString m_SessionSecret;        ///< Contains the session secret after a successful login.
  bool m_LoadInProgress;          ///< Flag to indicate that a request is in progress.
};

//==============================================================================
// Inline methods
//==============================================================================

//-----------------------------------------------------------------------------
inline QString FacebookLoginDialog::sessionKey() const
{
  return m_SessionKey;
}

//-----------------------------------------------------------------------------
inline QString FacebookLoginDialog::uid() const
{
  return m_Uid;
}

//-----------------------------------------------------------------------------
inline QString FacebookLoginDialog::sessionSecret() const
{
  return m_SessionSecret;
}

#endif // WEB_FACEBOOK_FACEBOOKLOGINDIALOG_H
