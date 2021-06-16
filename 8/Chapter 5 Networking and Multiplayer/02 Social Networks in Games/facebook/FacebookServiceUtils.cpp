//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#include "FacebookServiceUtils.h"

#include <QVector>
#include <QCryptographicHash>
#include <QNetworkReply>
#include <QXmlInputSource>
#include <QDomDocument>

//------------------------------------------------------------------------------
QString FacebookServiceUtils::sign(QVector<QString>& parameters,
  QString secret)
{
  // Parameters must be sorted.
  qSort(parameters);

  // Concatenate parameters and secret (no character between items).
  QByteArray data;
  for (int i = 0; i < parameters.size(); i++)
  {
    data.append(parameters[i]);
  }
  data.append(secret);

  // Create MD5 hash as lower-case hex string.
  QString signature = QCryptographicHash::hash(data, QCryptographicHash::Md5).toHex();
  return signature;
}

//------------------------------------------------------------------------------
void FacebookServiceUtils::createPostData(const QVector<QString>& parameters,
  QString signature, QByteArray& data)
{
  // Concatenate parameters and secret ('&' between items).
  for (int i = 0; i < parameters.size(); i++)
  {
    if (i > 0)
    {
      data.append("&");
    }
    data.append(parameters[i]);
  }
  data.append(QString("&sig=%1").arg(signature));
}

//------------------------------------------------------------------------------
FacebookServiceState FacebookServiceUtils::checkForError(QNetworkReply* reply,
  const QByteArray& replyData, QDomDocument& document)
{
  FacebookServiceState result = FacebookServiceStates::Ok;
  if (reply->error() > 0)
  {
    result = FacebookServiceStates::NetworkError;
  }
  else
  {
    // Turn result into XML document.
    QXmlInputSource is;
    is.setData(replyData);
    document.setContent(is.data());

    // Check for error responses.
    QDomNodeList errorCode = document.elementsByTagName("error_code");
    if (!errorCode.isEmpty())
    {
      QString error = errorCode.at(0).toElement().text();
      if (error == "200")
      {
        result = FacebookServiceStates::PermissionsError;
      }
      else if (error == "102")
      {
        result = FacebookServiceStates::SessionError;
      }
      else
      {
        result = FacebookServiceStates::NetworkError;
      }
    }
  }

  return result;
}

//------------------------------------------------------------------------------
int FacebookServiceUtils::generateCallId()
{
  static int counter = 0;
  return counter++;
}
