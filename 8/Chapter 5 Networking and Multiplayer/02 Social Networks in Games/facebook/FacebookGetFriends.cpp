//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#include "FacebookGetFriends.h"

#include <QDomDocument>
#include <QVector>

//------------------------------------------------------------------------------
/*static*/ const QString FacebookGetFriendsRequest::requestName("facebook.friends.get");

//------------------------------------------------------------------------------
FacebookServiceState FacebookGetFriendsRequest::operator()(const Input& /*input*/,
  QVector<QString>& /*parameters*/)
{
  // No input parameters.
  return FacebookServiceStates::Ok;
}

//------------------------------------------------------------------------------
FacebookServiceState FacebookGetFriendsResponse::operator()(
  const QDomDocument& document, Output& output)
{
  QDomNodeList uids = document.elementsByTagName("uid");
  output.clear();
  output.reserve(uids.count());
  for (int i = 0; i < uids.count(); i++)
  {
    output.append(uids.at(i).toElement().text());
  }

  return FacebookServiceStates::Ok;
}
