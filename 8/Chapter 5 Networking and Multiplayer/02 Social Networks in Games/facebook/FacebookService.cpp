//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#include "FacebookService.h"

#include <QNetworkReply>
#include <QThread>

//------------------------------------------------------------------------------
FacebookService::FacebookService(QString apiKey)
  : QObject()
  , m_Http()
  , m_EventLoop()
  , m_ApiKey(apiKey)
  , m_Secret()
  , m_AuthenticationToken()
  , m_SessionKey()
{
  connect(&m_Http, SIGNAL(finished(QNetworkReply*)),
          this, SLOT(requestFinished(QNetworkReply*)));
}

//------------------------------------------------------------------------------
FacebookService::~FacebookService()
{
}

//------------------------------------------------------------------------------
void FacebookService::requestFinished(QNetworkReply* reply)
{
  // Unlock event loop to let FacebokService know that the reply has arrived.
  m_EventLoop.exit();
}
