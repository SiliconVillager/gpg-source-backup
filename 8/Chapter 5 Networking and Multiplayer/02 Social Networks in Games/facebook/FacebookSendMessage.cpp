//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#include "FacebookSendMessage.h"

#include <QDomDocument>

//------------------------------------------------------------------------------
/*static*/ const QString FacebookSendMessageRequest::requestName("facebook.stream.publish");

//------------------------------------------------------------------------------
FacebookServiceState FacebookSendMessageRequest::operator()(const Input& input,
  QVector<QString>& parameters)
{
  parameters.append(QString("message=%1").arg(input));
  return FacebookServiceStates::Ok;
}

//------------------------------------------------------------------------------
FacebookServiceState FacebookSendMessageResponse::operator()(
  const QDomDocument& /*document*/, Output& /*output*/)
{
  // No output parameters.
  return FacebookServiceStates::Ok;
}
