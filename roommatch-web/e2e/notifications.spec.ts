import { test, expect, Page } from '@playwright/test';
import { fixtures, paged, session } from './fixtures';

async function notifications(page: Page, count=23) {
  await session(page);await fixtures(page);
  const rows=Array.from({length:count},(_,i)=>({idNotificacion:count-i,idUsuario:1,
    titulo:i===0?'Solicitud de contacto recibida':`Actividad de prueba ${count-i}`,
    mensaje:'Revisa la solicitud y decide si quieres compartir tus datos de contacto.',tipo:i%2?'contacto':'solicitud',
    leido:count===3?i===0:i<10,urlDestino:null,fechaCreacion:'2026-09-27T06:00:00'}));
  const state={rows,failList:false,failCount:false,failWrite:false,writes:0};
  const requests: URL[]=[];
  await page.route('**/api/notificaciones**',route=>{
    const url=new URL(route.request().url());requests.push(url);
    let data: unknown;
    if (url.pathname==='/api/notificaciones') {
      if(state.failList)return route.fulfill({status:503,contentType:'application/json',body:'{"status":"error"}'});
      const selected=url.searchParams.get('leido')==='false'?state.rows.filter(row=>!row.leido):state.rows;
      const number=Number(url.searchParams.get('page')??0),size=Number(url.searchParams.get('size')??10);
      const content=selected.slice(number*size,(number+1)*size);
      data={...paged(content),number,size,totalElements:selected.length,totalPages:Math.ceil(selected.length/size),last:(number+1)*size>=selected.length};
    } else if(url.pathname.endsWith('/no-leidas/count')) {
      if(state.failCount)return route.fulfill({status:503,contentType:'application/json',body:'{"status":"error"}'});
      data=state.rows.filter(row=>!row.leido).length;
    } else if(route.request().method()==='PUT') {
      state.writes++;
      if(state.failWrite)return route.fulfill({status:503,contentType:'application/json',body:'{"status":"error"}'});
      if(url.pathname.endsWith('/leer-todas')) {data=state.rows.filter(row=>!row.leido).length;state.rows.forEach(row=>row.leido=true);}
      else {const id=Number(url.pathname.split('/')[3]);const row=state.rows.find(row=>row.idNotificacion===id)!;row.leido=true;data=row;}
    } else throw new Error(`Unexpected notification route ${url.pathname}`);
    return route.fulfill({contentType:'application/json',body:JSON.stringify({status:'success',data})});
  });
  return {state,requests};
}

test('notifications: readable controls and states fit desktop and mobile under CSP',async({page},testInfo)=>{
  await notifications(page,3);const errors:string[]=[];page.on('pageerror',error=>errors.push(error.message));
  await page.goto('/notificaciones');await expect(page.getByRole('heading',{name:'Solicitud de contacto recibida'})).toBeVisible();
  await expect(page.locator('.unread-summary strong')).toHaveText('2');
  await expect(page.getByRole('heading',{level:1})).toHaveCount(1);
  expect(await page.evaluate(()=>document.documentElement.scrollWidth)).toBeLessThanOrEqual(page.viewportSize()!.width);
  expect(errors).toEqual([]);await testInfo.attach('notifications',{body:await page.screenshot({fullPage:true}),contentType:'image/png'});
});

test('unread filter reaches items beyond an entirely read first page and preserves server totals',async({page})=>{
  const {requests}=await notifications(page);await page.goto('/notificaciones');
  await expect(page.locator('.unread-summary strong')).toHaveText('13');await expect(page.locator('.notification-card')).toHaveCount(10);
  await expect(page.getByRole('button',{name:'Marcar todo como leído'})).toBeEnabled();
  await expect(page.getByRole('button',{name:/Marcar como leída:/})).toHaveCount(0);
  await page.getByRole('button',{name:'No leídas',exact:true}).click();await expect(page.getByText('13 sin leer',{exact:true})).toBeVisible();
  expect(requests.some(url=>url.searchParams.get('leido')==='false')).toBe(true);
  await page.getByRole('button',{name:'Siguiente',exact:true}).click();await expect(page.locator('.notification-card')).toHaveCount(3);
  await expect(page.getByText('Página 2 de 2')).toBeVisible();
  await page.getByRole('button',{name:'Marcar todo como leído'}).click();await expect(page.getByRole('heading',{name:'Estás al día'})).toBeVisible();
  await expect(page.locator('.unread-summary strong')).toHaveText('0');
});

test('failed list has a visible retry without a misleading empty state',async({page},testInfo)=>{
  const {state}=await notifications(page);state.failList=true;await page.goto('/notificaciones');
  await expect(page.getByText('No se pudieron cargar tus notificaciones. Intenta de nuevo.')).toBeVisible();
  await expect(page.getByText('Estás al día')).toHaveCount(0);await expect(page.getByText('Todavía no tienes notificaciones')).toHaveCount(0);
  await testInfo.attach('notifications-error',{body:await page.screenshot({fullPage:true}),contentType:'image/png'});
  state.failList=false;await page.getByRole('button',{name:'Reintentar notificaciones'}).click();await expect(page.locator('.notification-card')).toHaveCount(10);
});

test('a failed counter does not hide notifications or invent a zero',async({page})=>{
  const {state}=await notifications(page,3);state.failCount=true;await page.goto('/notificaciones');
  await expect(page.locator('.notification-card')).toHaveCount(3);await expect(page.locator('.unread-summary strong')).toHaveCount(0);
  await expect(page.getByRole('button',{name:'Marcar todo como leído'})).toBeDisabled();
  state.failCount=false;await page.getByRole('button',{name:'Reintentar contador'}).click();await expect(page.locator('.unread-summary strong')).toHaveText('2');
});

test('reading a notification is keyboard accessible and a rejected write keeps its state for retry',async({page})=>{
  const {state}=await notifications(page,3);state.failWrite=true;await page.goto('/notificaciones');
  const read=page.getByRole('button',{name:'Marcar como leída: Actividad de prueba 2',exact:true});
  await read.focus();await page.keyboard.press('Enter');
  await expect(page.getByText('No se pudo marcar la notificación como leída. Puedes volver a intentarlo.')).toBeVisible();
  await expect(read).toBeEnabled();await expect(page.locator('.unread-summary strong')).toHaveText('2');
  state.failWrite=false;await read.press('Enter');await expect(page.locator('.unread-summary strong')).toHaveText('1');expect(state.writes).toBe(2);
});
